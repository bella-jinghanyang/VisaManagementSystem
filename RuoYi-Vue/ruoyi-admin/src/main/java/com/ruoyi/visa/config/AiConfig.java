package com.ruoyi.visa.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 组件配置类（LangChain4j + Elasticsearch）
 *
 * <p>本配置类统一初始化 RAG 架构所需的三个核心 Bean：</p>
 * <ol>
 *   <li>{@link EmbeddingModel}：基于阿里云 DashScope text-embedding-v4 模型，
 *       将文本转化为 1536 维语义向量，替代原有手写 OkHttp Embedding 调用；</li>
 *   <li>{@link StreamingChatLanguageModel}：基于 DeepSeek-V3，
 *       以流式（SSE）模式生成签证咨询回答，替代原有手写 SSE 解析逻辑；</li>
 *   <li>{@link EmbeddingStore}：基于 Elasticsearch dense_vector 索引，
 *       提供 kNN ANN 向量检索，替代原有 MySQL 全表扫描方案。</li>
 * </ol>
 *
 * @author bella
 */
@Configuration
public class AiConfig {

    // ── DeepSeek 聊天模型配置 ──────────────────────────────────────────────
    @Value("${visa-ai.apiKey}")
    private String chatApiKey;

    @Value("${visa-ai.model}")
    private String chatModel;

    @Value("${visa-ai.baseUrl}")
    private String chatBaseUrl;

    // ── 阿里云 DashScope 向量模型配置 ─────────────────────────────────────
    @Value("${visa-ai.embeddingApiKey}")
    private String embeddingApiKey;

    @Value("${visa-ai.embeddingBaseUrl}")
    private String embeddingBaseUrl;

    @Value("${visa-ai.embeddingModel}")
    private String embeddingModel;

    // ── Elasticsearch 配置 ────────────────────────────────────────────────
    @Value("${elasticsearch.uris}")
    private String elasticsearchUri;

    @Value("${elasticsearch.indexName}")
    private String indexName;

    @Value("${elasticsearch.dimension}")
    private int dimension;

    /**
     * 语义向量化模型 Bean。
     *
     * <p>接入阿里云 DashScope OpenAI 兼容接口，模型 text-embedding-v4 输出 1536 维浮点向量。
     * 通过 LangChain4j {@link OpenAiEmbeddingModel} 封装，屏蔽底层 HTTP 协议细节，
     * 支持零代码切换至其他兼容 OpenAI 协议的向量模型。</p>
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(embeddingApiKey)
                .baseUrl(embeddingBaseUrl)
                .modelName(embeddingModel)
                .timeout(Duration.ofSeconds(60))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    /**
     * 流式聊天语言模型 Bean。
     *
     * <p>接入 DeepSeek-V3，启用流式（stream:true）模式，通过 LangChain4j
     * {@link OpenAiStreamingChatModel} 封装 SSE 流处理逻辑，
     * 调用方只需实现 {@code StreamingResponseHandler} 即可处理 token 推送与完成回调。</p>
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(chatApiKey)
                .baseUrl(chatBaseUrl)
                .modelName(chatModel)
                .timeout(Duration.ofSeconds(120))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    /**
     * Elasticsearch 低层 REST 客户端 Bean。
     *
     * <p>使用 Elasticsearch 官方 Java 客户端（8.x），以 HTTP 方式连接本地或远程 ES 节点。
     * 此客户端由 {@link ElasticsearchEmbeddingStore} 内部使用，不对业务代码直接暴露。</p>
     */
    @Bean
    public RestClient elasticsearchRestClient() {
        // 解析 http://host:port 形式的 URI
        String uri = elasticsearchUri.replaceFirst("https?://", "");
        String[] parts = uri.split(":");
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
        String scheme = elasticsearchUri.startsWith("https") ? "https" : "http";

        return RestClient.builder(new HttpHost(host, port, scheme)).build();
    }

    /**
     * Elasticsearch 高层 Java API 客户端 Bean。
     *
     * <p>基于 {@link RestClient} 构建，使用 Jackson 序列化映射。
     * 供 {@link ElasticsearchEmbeddingStore} 内部管理索引生命周期。</p>
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    /**
     * Elasticsearch 向量存储 Bean（RAG 检索核心）。
     *
     * <p>封装 Elasticsearch dense_vector 索引，提供：</p>
     * <ul>
     *   <li>{@code add(embedding, textSegment)}：写入知识块向量及元数据；</li>
     *   <li>{@code findRelevant(queryEmbedding, topK, minScore)}：kNN 语义检索。</li>
     * </ul>
     * <p>相比原有 MySQL 全表扫描方案，ES kNN 检索耗时不随知识库条目数线性增长，
     * 在万条规模下仍可保持亚百毫秒响应。</p>
     */
    @Bean
    public EmbeddingStore<TextSegment> elasticsearchEmbeddingStore(RestClient restClient) {
        return ElasticsearchEmbeddingStore.builder()
                .restClient(restClient)
                .indexName(indexName)
                .dimension(dimension)
                .build();
    }
}
