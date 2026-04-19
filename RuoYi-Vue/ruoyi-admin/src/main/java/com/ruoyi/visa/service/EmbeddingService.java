package com.ruoyi.visa.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 语义向量化服务（RAG 核心组件之一）
 *
 * <p>负责两项职责：</p>
 * <ol>
 *   <li>调用 Embedding API，将文本转化为高维浮点向量；</li>
 *   <li>提供余弦相似度计算方法，用于在检索阶段衡量查询向量与知识条目向量的语义距离。</li>
 * </ol>
 *
 * @author bella
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Value("${visa-ai.embeddingApiKey}")
    private String apiKey;

    @Value("${visa-ai.embeddingBaseUrl}")
    private String embeddingBaseUrl;

    @Value("${visa-ai.embeddingModel}")
    private String embeddingModel;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    // =========================================================
    //  公开方法
    // =========================================================

    /**
     * 将给定文本转化为语义向量。
     *
     * <p>调用符合 OpenAI 协议的 Embedding API（本系统默认接入 DeepSeek）。
     * 返回的浮点数组即为文本在高维语义空间中的坐标表示。</p>
     *
     * @param text 待向量化的文本（可为用户问题，也可为知识条目拼接后的文本）
     * @return 浮点向量列表；若 API 调用失败则返回空列表
     */
    public List<Double> embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", embeddingModel);
        requestBody.put("input", text);
        String endpoint = embeddingBaseUrl.endsWith("/")
                ? embeddingBaseUrl + "embeddings"
                : embeddingBaseUrl + "/embeddings";


        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        requestBody.toJSONString()))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.error("Embedding API 调用失败，状态码：{}", response.code());
                return Collections.emptyList();
            }
            JSONObject json = JSON.parseObject(response.body().string());
            JSONArray embeddingArray = json.getJSONArray("data")
                    .getJSONObject(0)
                    .getJSONArray("embedding");

            List<Double> vector = new ArrayList<>(embeddingArray.size());
            for (int i = 0; i < embeddingArray.size(); i++) {
                vector.add(embeddingArray.getDouble(i));
            }
            return vector;

        } catch (IOException e) {
            log.error("Embedding API 网络异常：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 将向量列表序列化为 JSON 字符串，用于持久化存储到数据库。
     */
    public String vectorToJson(List<Double> vector) {
        return JSON.toJSONString(vector);
    }

    /**
     * 将数据库中存储的 JSON 字符串反序列化为向量列表。
     */
    public List<Double> jsonToVector(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JSONArray arr = JSON.parseArray(json);
            List<Double> vector = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                vector.add(arr.getDouble(i));
            }
            return vector;
        } catch (Exception e) {
            log.warn("向量反序列化失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 计算两个向量的余弦相似度。
     *
     * <p>余弦相似度公式：cos(θ) = (A·B) / (|A| × |B|)，
     * 取值范围 [-1, 1]，值越接近 1 表示两段文本语义越相近。</p>
     *
     * @param v1 向量 A
     * @param v2 向量 B
     * @return 余弦相似度；若任一向量为空或维度不匹配则返回 0.0
     */
    public double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1 == null || v2 == null || v1.isEmpty() || v2.isEmpty() || v1.size() != v2.size()) {
            return 0.0;
        }

        double dotProduct  = 0.0;
        double normA       = 0.0;
        double normB       = 0.0;

        for (int i = 0; i < v1.size(); i++) {
            double a = v1.get(i);
            double b = v2.get(i);
            dotProduct += a * b;
            normA      += a * a;
            normB      += b * b;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}