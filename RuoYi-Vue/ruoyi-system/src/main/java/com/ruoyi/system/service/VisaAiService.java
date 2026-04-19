package com.ruoyi.system.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class VisaAiService {

    @Value("${visa-ai.apiKey}")
    private String apiKey;

    @Value("${visa-ai.model}")
    private String model;

    @Value("${visa-ai.baseUrl}")
    private String baseUrl;

    // 初始化 OkHttpClient，并设置较长的超时时间（AI生成比较慢，建议60秒）
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    /**
     * 调用 AI 接口
     * @param userMessage 用户的问题
     * @param context 检索到的知识库内容（小抄）
     * @return AI 的回答
     */
    public String getAiResponse(String userMessage, String context) {
        // 1. 构建系统提示词 (System Prompt) - 赋予 AI 身份并强制其使用参考资料
        String systemPrompt = "你是一个专业的跨境签证助手。\n" +
                "1. 当【参考资料】中包含用户问题的答案时，请严格根据资料给出精准回答；\n" +
                "2. 当【参考资料】中没有提到相关信息时，请利用你的通用知识回答用户，但必须在回答的开头或结尾明确标注：【以上回答基于通用政策，建议您咨询人工客服或以官方最新公布为准】；\n" +
                "3. 保持亲切、专业的口吻，回答要条理清晰。";

        // 2. 构建请求体 (符合 OpenAI 协议格式)
        JSONObject root = new JSONObject();
        root.put("model", model);

        JSONArray messages = new JSONArray();
        messages.add(createMessage("system", systemPrompt));

        String userPrompt;
        if (context != null && !context.isEmpty()) {
            // 有知识库内容时：拼接"参考资料 + 用户问题"
            userPrompt = "【参考资料】\n" + context + "\n\n【用户问题】\n" + userMessage;
        } else {
            // 没有检索到内容时：只发原始问题
            userPrompt = userMessage;
        }
        messages.add(createMessage("user", userPrompt));

        root.put("messages", messages);
        root.put("stream", false); // 暂时不使用流式，先用普通模式调试

        // 3. 发送请求
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                root.toJSONString()
                );

        Request request = new Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无错误详情";
                System.err.println("【AI接口报错】状态码：" + response.code());
                System.err.println("【AI接口报错】错误内容：" + errorBody);

                // 针对常见错误的温馨提示
                if (response.code() == 401) return "AI 认证失败：API Key 可能填错或失效了。";
                if (response.code() == 402) return "AI 余额不足：请检查账户充值情况。";
                if (response.code() == 429) return "AI 请求太频繁：请稍后再试。";

                return "AI 大脑现在有点忙（错误码：" + response.code() + "），请稍后再试。";
            }
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                // 解析返回的 JSON
                JSONObject jsonRes = JSON.parseObject(responseBody);
                return jsonRes.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else {
                return "AI 接口调用失败，错误码：" + response.code();
            }
        } catch (IOException e) {
            System.err.println("【网络异常】无法连接到 DeepSeek 服務器：" + e.getMessage());
            return "连接大脑超时，可能是因为您问的问题太难，或者网络不太好。";
        }
    }

    private JSONObject createMessage(String role, String content) {
        JSONObject msg = new JSONObject();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }
}