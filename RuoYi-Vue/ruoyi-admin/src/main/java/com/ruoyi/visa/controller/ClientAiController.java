package com.ruoyi.visa.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.visa.service.EmbeddingService;
import com.ruoyi.system.service.VisaAiService;
import com.ruoyi.visa.domain.OrderMessage;
import com.ruoyi.visa.service.IOrderMessageService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Comparator;
import java.util.List;

/**
 * 智能签证助手 API（RAG 架构 · Elasticsearch 版）
 *
 * <p>检索流程（重构后）：</p>
 * <ol>
 *   <li>通过 LangChain4j {@link EmbeddingService} 将用户问题向量化；</li>
 *   <li>调用 {@link EmbeddingStore}（ElasticsearchEmbeddingStore）执行 kNN 语义检索，
 *       直接返回 Top-K 最相关文本块，无需全表扫描；</li>
 *   <li>将检索结果拼装为上下文字符串，注入 LLM Prompt；</li>
 *   <li>通过 {@link VisaAiService} 以 SSE 流式协议推送 DeepSeek-V3 回答。</li>
 * </ol>
 *
 * <p>相比原实现，本版本将 RAG 检索的时间复杂度从 O(N) 降至近似 O(log N)，
 * 在知识库规模增长后仍能保持亚百毫秒级检索响应。</p>
 *
 * @author bella
 */
@RestController
@RequestMapping("/client/ai")
public class ClientAiController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ClientAiController.class);

    /** RAG 检索阶段返回的最大知识块数 */
    @Value("${visa-ai.topK:3}")
    private int topK;

    /** 余弦相似度最低阈值，低于此值的块视为无关，不纳入上下文 */
    private static final double SIMILARITY_THRESHOLD = 0.3;

    @Autowired
    private VisaAiService aiService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private IOrderMessageService messageService;

    /**
     * AI 智能咨询接口（SSE 流式输出）
     *
     * <p>接口采用 Server-Sent Events 协议，返回 {@code text/event-stream} 响应流。
     * 每个 SSE 事件携带一个 JSON 编码的文本 token；当收到 {@code data: [DONE]} 事件时
     * 表示模型生成完毕，前端应关闭 {@link java.util.EventSource} 连接。</p>
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

        // ── 步骤 2：创建 SseEmitter，超时时间与 LLM readTimeout 对齐 ─────
        SseEmitter emitter = new SseEmitter(VisaAiService.SSE_TIMEOUT_MS);

        // ── 步骤 3：后台线程执行 RAG 检索 + 模型调用 ───────────────────────
        new Thread(() -> {
            String context = retrieveContext(q);
            log.info("RAG 检索上下文：{}", context);

            aiService.streamAiResponse(q, context, emitter, fullText -> {
                // ── 步骤 4：流结束后持久化完整 AI 回答 ────────────────────
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
    //  私有方法：RAG 检索阶段（Elasticsearch kNN 版）
    // =========================================================

    /**
     * 基于 Elasticsearch kNN 向量检索，返回最相关知识块并拼装为 LLM 上下文字符串。
     *
     * <p>算法步骤：</p>
     * <ol>
     *   <li>将用户问题通过 LangChain4j {@link EmbeddingService} 向量化；</li>
     *   <li>调用 {@link EmbeddingStore#findRelevant} 执行 Elasticsearch kNN ANN 检索，
     *       直接返回 Top-K 相关文本块，无需加载全量知识库到内存；</li>
     *   <li>过滤低于相似度阈值的块，将命中块的标题与文本拼接为上下文字符串。</li>
     * </ol>
     *
     * @param question 用户的原始问题
     * @return 检索到的上下文文本；知识库无相关内容时返回空字符串
     */
    private String retrieveContext(String question) {
        // 1. 对用户问题进行向量化
        Embedding queryEmbedding = embeddingService.embed(question);
        if (queryEmbedding.vector().length == 0) {
            // Embedding API 不可用时降级为空上下文，由 LLM 凭通用知识回答
            log.warn("问题向量化失败，降级为空上下文：question={}", question);
            return "";
        }

        // 2. 调用 Elasticsearch kNN 检索，取相似度 >= SIMILARITY_THRESHOLD 的 Top-K 块
        List<EmbeddingMatch<TextSegment>> matches;
        try {
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(topK)
                    .minScore(SIMILARITY_THRESHOLD)
                    .build();
            matches = embeddingStore.search(searchRequest).matches();
        } catch (Exception e) {
            // 知识库索引尚未创建（首次部署未摄取任何知识）或 ES 暂时不可用，降级为空上下文
            log.warn("Elasticsearch 检索失败，降级为空上下文（LLM 将凭通用知识回答）：{}", e.getMessage());
            return "";
        }

        if (matches.isEmpty()) {
            log.info("知识库中未检索到相关内容：question={}", question);
            return "";
        }

        // 3. 拼装上下文字符串，注入 Prompt
        StringBuilder ctx = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : matches) {
            TextSegment segment = match.embedded();
            String title    = segment.metadata().getString("title");
            String category = segment.metadata().getString("category");
            log.info("命中知识块：title={}, category={}, score={}",
                    title, category, match.score());
            ctx.append("【").append(title != null ? title : "相关知识").append("】\n");
            ctx.append(segment.text()).append("\n---\n");
        }
        return ctx.toString();
    }
}