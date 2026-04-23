package com.ruoyi.visa.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.visa.service.EmbeddingService;
import com.ruoyi.system.service.VisaAiService;
import com.ruoyi.visa.domain.OrderMessage;
import com.ruoyi.visa.domain.VisaKnowledge;
import com.ruoyi.visa.service.IOrderMessageService;
import com.ruoyi.visa.service.IVisaKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.ruoyi.framework.datasource.DynamicDataSourceContextHolder.log;

/**
 * 智能签证助手 API（RAG 架构）
 *
 * <p>检索流程：
 * <ol>
 *   <li>将用户问题通过 Embedding API 转化为查询向量；</li>
 *   <li>加载知识库全量条目，逐一计算与查询向量的余弦相似度；</li>
 *   <li>取相似度最高的 Top-K 条目作为上下文（Context）；</li>
 *   <li>将 Context 与问题拼装为 Prompt，调用大语言模型生成最终回答。</li>
 * </ol>
 * </p>
 *
 * @author bella
 */
@RestController
@RequestMapping("/client/ai")
public class ClientAiController extends BaseController {

    /** RAG 检索阶段返回的最大知识条目数 */
    @Value("${visa-ai.topK:3}")
    private int topK;

    /** 余弦相似度最低阈值，低于此值的条目视为无关，不纳入上下文 */
    private static final double SIMILARITY_THRESHOLD = 0.3;

    @Autowired
    private VisaAiService aiService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private IVisaKnowledgeService knowledgeService;

    @Autowired
    private IOrderMessageService messageService;

    /**
     * AI 智能咨询接口（SSE 流式输出）
     *
     * <p>接口采用 Server-Sent Events 协议，返回 {@code text/event-stream} 响应流。
     * 每个 SSE 事件携带一个 JSON 编码的文本 token；当收到 {@code data: [DONE]} 事件时
     * 表示模型生成完毕，前端应关闭 {@link EventSource} 连接。</p>
     *
     * <p>处理流程：
     * <ol>
     *   <li>同步持久化用户消息；</li>
     *   <li>在后台线程中执行 RAG 检索与模型调用，避免阻塞 Servlet 线程；</li>
     *   <li>每个 token 到达时立即通过 {@link SseEmitter} 推送，首字节延迟
     *       由大模型推理速度决定（通常 500ms～1.5s），满足 Miller 两秒交互阈值；</li>
     *   <li>流结束后将完整回答一次性写入数据库。</li>
     * </ol>
     * </p>
     *
     * @param q          用户问题
     * @param orderId    关联订单 ID（可选，为 0 时表示通用咨询）
     * @param customerId 当前客户 ID（可选）
     * @return SSE 发射器（Spring 持有连接直至 emitter.complete() 被调用）
     */
    @Anonymous
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam("q")                                  String q,
                           @RequestParam(value = "orderId",    required = false) Long   orderId,
                           @RequestParam(value = "customerId", required = false) Long   customerId) {

        long oid = orderId    != null ? orderId    : 0L;
        long cid = customerId != null ? customerId : 0L;

        // ── 步骤 1：同步持久化用户消息 ────────────────────────────────────
        OrderMessage userMsg = new OrderMessage();
        userMsg.setOrderId(oid);
        userMsg.setCustomerId(cid);
        userMsg.setSenderType(1);
        userMsg.setContent(q);
        userMsg.setIsAi("1");
        messageService.insertOrderMessage(userMsg);

        // ── 步骤 2：创建 SseEmitter，超时时间与 OkHttp readTimeout 对齐 ──
        SseEmitter emitter = new SseEmitter(VisaAiService.SSE_TIMEOUT_MS);

        // ── 步骤 3：后台线程执行 RAG + 模型调用，避免阻塞 Servlet 线程 ──
        new Thread(() -> {
            String context = retrieveContext(q);
            log.info("检索到的上下文内容: {}", context);

            aiService.streamAiResponse(q, context, emitter, fullText -> {
                // ── 步骤 4：流结束后持久化完整 AI 回答 ────────────────
                OrderMessage aiMsg = new OrderMessage();
                aiMsg.setOrderId(oid);
                aiMsg.setCustomerId(cid);
                aiMsg.setSenderType(2);
                aiMsg.setContent(fullText);
                aiMsg.setIsAi("1");
                messageService.insertOrderMessage(aiMsg);
            });
        }).start();

        return emitter;
    }

    /**
     * 获取历史聊天记录
     */
    @Anonymous
    @GetMapping("/history")
    public AjaxResult history(
            @RequestParam(value = "orderId",    required = false) Long orderId,
            @RequestParam(value = "customerId", required = false) Long customerId) {

        OrderMessage query = new OrderMessage();
        query.setOrderId(orderId != null ? orderId : 0L);
        if (customerId != null) {
            query.setCustomerId(customerId);
        }
        List<OrderMessage> list = messageService.selectOrderMessageList(query);
        list.sort(Comparator.comparing(OrderMessage::getCreateTime));
        return success(list);
    }

    // =========================================================
    //  私有方法：RAG 检索阶段
    // =========================================================

    /**
     * 基于语义向量检索最相关的知识条目，并拼装为 LLM 可直接使用的上下文字符串。
     *
     * <p>算法步骤：
     * <ol>
     *   <li>将用户问题向量化（调用 Embedding API）；</li>
     *   <li>从数据库加载全部有效知识条目（含预存向量）；</li>
     *   <li>计算每条知识与查询向量的余弦相似度；</li>
     *   <li>按相似度降序排序，取 Top-K 且高于阈值的条目；</li>
     *   <li>将命中条目的标题与内容拼接为纯文本，返回给 VisaAiService。</li>
     * </ol>
     * </p>
     *
     * @param question 用户的原始问题
     * @return 检索到的上下文文本；若知识库中无相关内容则返回空字符串
     */
    private String retrieveContext(String question) {
        // 1. 对用户问题进行向量化
        List<Double> queryVector = embeddingService.embed(question);
        if (queryVector.isEmpty()) {
            // Embedding API 不可用时，降级为空上下文，让 LLM 凭通用知识回答
            return "";
        }

        // 2. 加载知识库全量条目（含向量字段）
        List<VisaKnowledge> allDocs = knowledgeService.selectAllActiveWithEmbedding();
        if (allDocs.isEmpty()) {
            return "";
        }

        // 3. 计算余弦相似度并按降序排序
        List<VisaKnowledge> ranked = allDocs.stream()
                .filter(doc -> doc.getEmbedding() != null && !doc.getEmbedding().isEmpty())
                .sorted(Comparator.comparingDouble((VisaKnowledge doc) -> {
                    List<Double> docVector = embeddingService.jsonToVector(doc.getEmbedding());
                    return embeddingService.cosineSimilarity(queryVector, docVector);
                }).reversed())
                .limit(topK)
                .collect(Collectors.toList());

        // 4. 过滤低相似度结果，并拼装上下文文本
        StringBuilder ctx = new StringBuilder();
        for (VisaKnowledge doc : ranked) {
            List<Double> docVector = embeddingService.jsonToVector(doc.getEmbedding());
            double sim = embeddingService.cosineSimilarity(queryVector, docVector);
            log.info("知识条目：{}，相似度分数：{}", doc.getTitle(), sim);
            if (sim >= SIMILARITY_THRESHOLD) {
                ctx.append("【").append(doc.getTitle()).append("】\n");
                ctx.append(doc.getContent()).append("\n---\n");
            }
        }
        return ctx.toString();
    }
}