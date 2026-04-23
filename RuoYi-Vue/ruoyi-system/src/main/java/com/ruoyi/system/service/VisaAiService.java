package com.ruoyi.system.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import okio.BufferedSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class VisaAiService {

    @Value("${visa-ai.apiKey}")
    private String apiKey;

    @Value("${visa-ai.model}")
    private String model;

    @Value("${visa-ai.baseUrl}")
    private String baseUrl;

    /** SSE 连接保持时间上限（毫秒），与 OkHttp readTimeout 对齐 */
    private static final long SSE_TIMEOUT_MS = 120_000L;

    /** SSE 连接保持时间上限（毫秒），与 OkHttp readTimeout 对齐 */
    public static final long SSE_TIMEOUT_MS = 120_000L;

    private static final String SYSTEM_PROMPT =
            "你是一个专业的跨境签证助手。\n" +
            "1. 当【参考资料】中包含用户问题的答案时，请严格根据资料给出精准回答；\n" +
            "2. 当【参考资料】中没有提到相关信息时，请利用你的通用知识回答用户，但必须在回答的开头或结尾明确标注：" +
            "【以上回答基于通用政策，建议您咨询人工客服或以官方最新公布为准】；\n" +
            "3. 保持亲切、专业的口吻，回答要条理清晰。";

    // 流式读取需要足够长的 readTimeout，connectTimeout 可以短一些
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    /**
     * 流式调用大语言模型（SSE 模式）。
     *
     * <p>方法向大模型发起 {@code stream:true} 请求，逐行解析 OpenAI 兼容协议的
     * Server-Sent Events 数据流，将每个 token 通过 {@link SseEmitter} 实时推送给浏览器，
     * 同时在本地累积完整回答文本。流结束后：
     * <ol>
     *   <li>向客户端发送终止信号 {@code [DONE]}；</li>
     *   <li>调用 {@code onComplete} 回调，将完整文本交由调用方持久化；</li>
     *   <li>调用 {@code emitter.complete()} 关闭 SSE 连接。</li>
     * </ol>
     * </p>
     *
     * @param userMessage 用户的原始问题
     * @param context     RAG 检索阶段拼装好的知识库上下文（可为空）
     * @param emitter     Spring SSE 发射器，用于向浏览器推送分块数据
     * @param onComplete  流结束后的回调，接收完整 AI 回答文本，供调用方写库
     */
    public void streamAiResponse(String userMessage, String context,
                                 SseEmitter emitter, Consumer<String> onComplete) {

        // 1. 构建请求体（符合 OpenAI 兼容协议，开启流式模式）
        JSONObject root = new JSONObject();
        root.put("model", model);
        root.put("stream", true);

        JSONArray messages = new JSONArray();
        messages.add(createMessage("system", SYSTEM_PROMPT));

        String userPrompt = (context != null && !context.isEmpty())
                ? "【参考资料】\n" + context + "\n\n【用户问题】\n" + userMessage
                : userMessage;
        messages.add(createMessage("user", userPrompt));
        root.put("messages", messages);

        // 2. 构建 HTTP 请求
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                root.toJSONString());

        Request request = new Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        StringBuilder fullText = new StringBuilder();

        try (Response response = client.newCall(request).execute()) {
            // 3. 处理非 2xx 响应
            if (!response.isSuccessful()) {
                System.err.println("【AI接口报错】状态码：" + response.code());
                String errMsg = resolveErrorMessage(response.code());
                sendChunkAndDone(emitter, errMsg);
                onComplete.accept(errMsg);
                return;
            }

            // 4. 逐行读取 SSE 流，解析 delta.content 并推送给浏览器
            BufferedSource source = response.body().source();
            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line == null) {
                    break;
                }
                if (!line.startsWith("data: ")) {
                    continue;
                }
                String data = line.substring(6).trim();
                if ("[DONE]".equals(data)) {
                    break;
                }
                try {
                    JSONObject obj = JSON.parseObject(data);
                    JSONArray choices = obj.getJSONArray("choices");
                    if (choices == null || choices.isEmpty()) {
                        continue;
                    }
                    JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                    if (delta == null) {
                        continue;
                    }
                    String chunk = delta.getString("content");
                    if (chunk != null && !chunk.isEmpty()) {
                        fullText.append(chunk);
                        // JSON 编码后再发送，保证换行符等特殊字符在 SSE 中安全传输
                        emitter.send(SseEmitter.event().data(JSON.toJSONString(chunk)));
                    }
                } catch (Exception e) {
                    System.err.println("【SSE 解析异常】跳过当前行: " + e.getMessage());
                }
            }

            // 5. 流结束：通知前端并持久化完整回答
            emitter.send(SseEmitter.event().data("[DONE]"));
            emitter.complete();
            onComplete.accept(fullText.toString());

        } catch (IOException e) {
            System.err.println("【网络异常】无法连接到 AI 服务器：" + e.getMessage());
            String errMsg = "连接 AI 超时，请稍后重试。";
            try {
                sendChunkAndDone(emitter, errMsg);
            } catch (IOException sendErr) {
                System.err.println("【SSE 推送异常】向客户端发送错误消息失败：" + sendErr.getMessage());
            }
            onComplete.accept(errMsg);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  私有工具方法
    // ──────────────────────────────────────────────────────────────────────────

    private void sendChunkAndDone(SseEmitter emitter, String text) throws IOException {
        emitter.send(SseEmitter.event().data(JSON.toJSONString(text)));
        emitter.send(SseEmitter.event().data("[DONE]"));
        emitter.complete();
    }

    private String resolveErrorMessage(int code) {
        if (code == 401) return "AI 认证失败：API Key 可能填错或失效了。";
        if (code == 402) return "AI 余额不足：请检查账户充值情况。";
        if (code == 429) return "AI 请求太频繁：请稍后再试。";
        return "AI 大脑现在有点忙（错误码：" + code + "），请稍后再试。";
    }

    private JSONObject createMessage(String role, String content) {
        JSONObject msg = new JSONObject();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }
}