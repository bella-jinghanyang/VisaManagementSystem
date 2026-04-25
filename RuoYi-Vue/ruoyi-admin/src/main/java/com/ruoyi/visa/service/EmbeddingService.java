package com.ruoyi.visa.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 语义向量化服务（RAG 核心组件之一）
 *
 * <p>原实现通过手写 OkHttp 请求调用 Embedding API，存在代码量大、
 * 难以切换模型、异常处理分散等问题。</p>
 *
 * <p>重构后本类作为 LangChain4j {@link EmbeddingModel} 的轻量适配层，
 * 将底层 HTTP 协议细节完全交由框架处理：</p>
 * <ul>
 *   <li>模型切换：仅需修改 {@code AiConfig} 中的配置，无需改动此类；</li>
 *   <li>向量化：调用 {@link #embed(String)} 即可获得 LangChain4j {@link Embedding} 对象；</li>
 *   <li>向量检索：已由 {@code ElasticsearchEmbeddingStore} 的 kNN 接口接管，
 *       原手写余弦相似度方法不再需要。</li>
 * </ul>
 *
 * @author bella
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 将给定文本转化为语义向量（LangChain4j {@link Embedding} 类型）。
     *
     * <p>内部委托给 {@link EmbeddingModel}（当前为阿里云 DashScope text-embedding-v4），
     * 框架自动处理重试、超时、响应解析等细节。</p>
     *
     * @param text 待向量化的文本（可为用户问题或知识条目拼接文本）
     * @return {@link Embedding} 对象，包含 float[] 向量；API 调用失败时返回零向量
     */
    public Embedding embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Embedding.from(new float[0]);
        }
        try {
            Response<Embedding> response = embeddingModel.embed(text);
            return response.content();
        } catch (Exception e) {
            log.error("EmbeddingModel 调用失败：{}", e.getMessage(), e);
            return Embedding.from(new float[0]);
        }
    }

    /**
     * 将向量序列化为 JSON 字符串，用于写入 MySQL embedding 备份字段。
     *
     * <p>此方法保持原有接口签名，供 {@link DocumentIngestionService} 调用，
     * 确保 MySQL 字段与 Elasticsearch 向量索引保持一致性备份。</p>
     *
     * @param vector float 数组形式的语义向量
     * @return JSON 数组字符串，如 {@code [0.12, -0.34, ...]}
     */
    public String vectorToJson(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
