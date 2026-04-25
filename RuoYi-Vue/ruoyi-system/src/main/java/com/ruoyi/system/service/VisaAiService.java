package com.ruoyi.system.service;

import com.alibaba.fastjson2.JSON;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 签证智能助手流式对话服务（LangChain4j 重构版）
 *
 * <p>原实现通过手写 OkHttp + SSE 行解析完成流式调用，存在两个问题：</p>
 * <ol>
 *   <li>重复字段声明（{@code SSE_TIMEOUT_MS} 同时存在 private 和 public 修饰符）
 *       导致编译错误；</li>
 *   <li>约 80 行手写 HTTP 及 SSE 解析代码，切换模型成本高。</li>
 * </ol>
 *
 * <p>重构后委托给 LangChain4j {@link StreamingChatLanguageModel}（当前接入 DeepSeek-V3），
 * 框架自动处理 SSE 流解析、token 回调、错误处理等细节，
 * 业务逻辑仅需实现 {@link StreamingResponseHandler} 的三个方法：
 * {@code onNext}（token 推送）、{@code onComplete}（持久化完整回答）、
 * {@code onError}（异常降级）。</p>
 *
 * @author bella
 */
@Service
public class VisaAiService {

    private static final Logger log = LoggerFactory.getLogger(VisaAiService.class);

    /**
     * SSE 连接保持时间上限（毫秒），与 {@link SseEmitter} 超时参数对齐。
     * 公开声明供 {@code ClientAiController} 初始化 SseEmitter 时引用。
     */
    public static final long SSE_TIMEOUT_MS = 120_000L;




    private static final String SYSTEM_PROMPT =
            "你是一个专业的跨境签证助手。\n" +
            "1. 当【参考资料】中包含用户问题的答案时，请严格根据资料给出精准回答；\n" +
            "2. 当【参考资料】中没有提到相关信息时，请利用你的通用知识回答用户，" +
            "但必须在回答的开头或结尾明确标注：" +
            "【以上回答基于通用政策，建议您咨询人工客服或以官方最新公布为准】；\n" +
            "3. 保持亲切、专业的口吻，回答要条理清晰。";

    @Autowired
    private StreamingChatLanguageModel streamingChatModel;

    /**
     * 流式调用大语言模型（SSE 模式）。
     *
     * <p>通过 LangChain4j {@link StreamingChatLanguageModel} 以非阻塞方式向 DeepSeek-V3
     * 发起 {@code stream:true} 请求，框架内部解析 Server-Sent Events 数据流：</p>
     * <ul>
     *   <li>每个 token 到达时触发 {@code onNext}，立即通过 {@link SseEmitter} 推送至浏览器；</li>
     *   <li>生成完毕时触发 {@code onComplete}，发送终止信号并回调 {@code onComplete} 持久化回答；</li>
     *   <li>发生异常时触发 {@code onError}，向客户端推送友好错误提示并关闭连接。</li>
     * </ul>
     *
     * @param userMessage 用户的原始问题
     * @param context     RAG 检索阶段拼装好的知识库上下文（可为空字符串）
     * @param emitter     Spring SSE 发射器，用于向浏览器推送分块数据
     * @param onComplete  流结束后的回调，接收完整 AI 回答文本，供调用方持久化到数据库
     */
    public void streamAiResponse(String userMessage, String context,
                                 SseEmitter emitter, Consumer<String> onComplete) {

        String userPrompt = (context != null && !context.isEmpty())
                ? "【参考资料】\n" + context + "\n\n【用户问题】\n" + userMessage
                : userMessage;

        List<ChatMessage> messages = Arrays.asList(
                SystemMessage.from(SYSTEM_PROMPT),
                UserMessage.from(userPrompt)
        );

        StringBuilder fullText = new StringBuilder();

        streamingChatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {

            /**
             * 每个 token 到达时回调，立即 JSON 编码后通过 SSE 推送至浏览器。
             * JSON 编码确保换行符、引号等特殊字符在 SSE 协议中安全传输。
             */
            @Override
            public void onNext(String token) {
                try {
                    fullText.append(token);
                    emitter.send(SseEmitter.event().data(JSON.toJSONString(token)));
                } catch (IOException e) {
                    log.error("SSE token 推送失败：{}", e.getMessage());
                    emitter.completeWithError(e);
                }
            }

            /**
             * 生成完毕时回调：向前端发送终止信号，关闭 SSE 连接，并持久化完整回答。
             */
            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    emitter.send(SseEmitter.event().data("[DONE]"));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("SSE 完成信号推送失败：{}", e.getMessage());
                }
                onComplete.accept(fullText.toString());
            }

            /**
             * 发生异常时回调：向前端推送友好错误文案并关闭连接。
             */
            @Override
            public void onError(Throwable throwable) {
                log.error("LLM 流式生成异常：{}", throwable.getMessage(), throwable);
                String errMsg = resolveErrorMessage(throwable);
                try {
                    emitter.send(SseEmitter.event().data(JSON.toJSONString(errMsg)));
                    emitter.send(SseEmitter.event().data("[DONE]"));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
                onComplete.accept(errMsg);
            }
        });
    }

    // =========================================================
    //  私有工具方法
    // =========================================================

    /**
     * 将异常转化为用户可读的友好错误文案。
     */
    private String resolveErrorMessage(Throwable throwable) {
        String msg = throwable.getMessage();
        if (msg == null) {
            return "AI 服务暂时不可用，请稍后重试。";
        }
        if (msg.contains("401")) return "AI 认证失败：API Key 可能填错或失效了。";
        if (msg.contains("402")) return "AI 余额不足：请检查账户充值情况。";
        if (msg.contains("429")) return "AI 请求太频繁：请稍后再试。";
        if (msg.contains("timeout") || msg.contains("timed out")) return "连接 AI 超时，请稍后重试。";
        return "AI 大脑现在有点忙，请稍后再试。";
    }
}