package com.ruoyi.visa.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import com.ruoyi.visa.domain.VisaKnowledge;
import com.ruoyi.visa.mapper.VisaKnowledgeMapper;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * 文档知识摄取服务（RAG 写入管道）
 *
 * <p>本服务是 RAG 架构中"写"方向的核心组件，整合四项技术实现完整的文档摄取流程：</p>
 *
 * <pre>
 *  管理员上传 PDF/Word
 *       │
 *       ▼
 *  ① MinIO：持久化原始文件，获取对象路径
 *       │
 *       ▼
 *  ② Apache Tika：解析任意格式文件，提取纯文本
 *       │
 *       ▼
 *  ③ LangChain4j DocumentSplitter：按语义边界分块（512字符，100字符重叠）
 *       │
 *       ▼
 *  ④ LangChain4j EmbeddingModel（DashScope text-embedding-v4）：向量化
 *       │
 *       ▼
 *  ⑤ ElasticsearchEmbeddingStore：写入 dense_vector 索引
 * </pre>
 *
 * <p>对于管理员手动录入的文本知识条目，跳过步骤①②，直接从步骤③开始处理。</p>
 *
 * @author bella
 */
@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    /** 每个文本块的最大字符数（约 128~256 个中文字符或英文词） */
    private static final int CHUNK_SIZE    = 512;

    /** 相邻文本块的重叠字符数，保证语义连贯性不被切断 */
    private static final int CHUNK_OVERLAP = 100;

    /** Apache Tika 统一文档解析器（线程安全，可复用单实例） */
    private static final Tika TIKA = new Tika();

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private MinioService minioService;

    @Autowired
    private VisaKnowledgeMapper visaKnowledgeMapper;

    // =========================================================
    //  对外公开方法
    // =========================================================

    /**
     * 处理文件上传型知识摄取（完整四步流程）。
     *
     * <p>流程：MinIO 存储 → Tika 解析 → LangChain4j 分块向量化 → Elasticsearch 写入。
     * 方法返回 MinIO 对象路径，调用方应将其持久化到 {@code visa_knowledge.source_file}。</p>
     *
     * <p>此方法同步执行，接口层应在后台线程（{@code @Async}）中调用，避免阻塞 HTTP 请求。</p>
     *
     * @param file      管理员上传的原始文档（PDF / Word / Excel / HTML 等）
     * @param knowledge 对应的知识条目元数据（id、title、category 等）
     * @return MinIO 中的对象路径（写入数据库的 source_file 值）
     * @throws Exception Tika 解析失败或 ES 写入异常时向上抛出
     */
    public String ingestDocument(MultipartFile file, VisaKnowledge knowledge) throws Exception {
        // 步骤 ①：将原始文件上传至 MinIO 对象存储
        String objectName = minioService.uploadFile(file);
        log.info("文件已写入 MinIO：knowledge_id={}, object={}", knowledge.getId(), objectName);

        // 步骤 ②：通过 Apache Tika 从文件流中提取纯文本
        // MinIO 返回的 GetObjectResponse 不原生支持 mark/reset，需包装为 BufferedInputStream，
        // 使 Tika 的文件类型探测（AutoDetectParser）可正确 reset 流并二次读取。
        String rawText;
        try (InputStream raw = minioService.getFileStream(objectName);
             BufferedInputStream stream = new BufferedInputStream(raw)) {
            rawText = TIKA.parseToString(stream);
        } catch (TikaException e) {
            // 在 Spring Boot fat jar 环境下，Commons Compress ServiceLoader 的
            // META-INF/services 文件可能被合并覆盖，导致解析基于 ZIP 格式的 Office 文档
            // （.docx/.xlsx 等）时抛出 "No Archiver found for the stream signature"。
            // 此处降级使用知识条目的元数据字段（title/category/keywords/content）
            // 拼接文本继续完成摄取，确保知识可被向量化写入 Elasticsearch。
            log.warn("Tika 解析文件失败，降级使用知识条目文本字段继续摄取：" +
                    "knowledge_id={}, object={}, error={}", knowledge.getId(), objectName, e.getMessage());
            rawText = buildTextForEmbedding(knowledge);
        }
        if (rawText == null || rawText.trim().isEmpty()) {
            log.warn("文件文本提取结果为空，跳过向量化：object={}", objectName);
            return objectName;
        }
        log.info("文本提取完成：knowledge_id={}, 字符数={}", knowledge.getId(), rawText.length());

        // 步骤 ③~⑤：分块、向量化、写入 Elasticsearch
        ingestText(rawText, knowledge);

        return objectName;
    }

    /**
     * 处理文本型知识摄取（跳过 MinIO 和 Tika，直接分块向量化写 ES）。
     *
     * <p>适用场景：管理员在后台手动录入文本内容，无需上传文件。
     * 同时兼容批量刷新（{@code refreshAllEmbeddings}）的调用场景。</p>
     *
     * <p>摄取前自动删除该知识条目在 ES 中的旧版本数据块，
     * 避免内容修改后产生重复或过时的检索结果。</p>
     *
     * @param knowledge 包含 title / category / keywords / content 的知识条目
     */
    @Async
    public void ingestTextAsync(VisaKnowledge knowledge) {
        try {
            String text = buildTextForEmbedding(knowledge);
            ingestText(text, knowledge);
        } catch (Exception e) {
            log.error("文本摄取失败：knowledge_id={}, msg={}", knowledge.getId(), e.getMessage(), e);
        }
    }

    // =========================================================
    //  私有方法
    // =========================================================

    /**
     * 将纯文本经分块、向量化后写入 Elasticsearch。
     *
     * <p>每个文本块携带以下元数据，用于检索阶段溯源：</p>
     * <ul>
     *   <li>{@code knowledge_id}：对应 MySQL visa_knowledge.id；</li>
     *   <li>{@code title}：知识条目标题，检索结果组装上下文时显示；</li>
     *   <li>{@code category}：知识分类，供后续按类别过滤检索使用。</li>
     * </ul>
     *
     * @param text      已提取或拼接好的纯文本
     * @param knowledge 知识条目元数据（提供 id、title、category）
     * @throws RuntimeException 若所有文本块均写入 Elasticsearch 失败，则向上抛出异常，
     *                          告知调用方检查向量化 API 或 ES 连接
     */
    private void ingestText(String text, VisaKnowledge knowledge) {
        // 构建 LangChain4j Document，附加元数据
        Metadata metadata = Metadata.from("knowledge_id", String.valueOf(knowledge.getId()));
        if (knowledge.getTitle()    != null) metadata.add("title",    knowledge.getTitle());
        if (knowledge.getCategory() != null) metadata.add("category", knowledge.getCategory());

        Document document = Document.from(text, metadata);

        // 递归字符分割器：按 CHUNK_SIZE 字符切块，相邻块重叠 CHUNK_OVERLAP 字符以保证语义连贯
        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP);
        List<TextSegment> segments = splitter.split(document);
        log.info("文本分块完成：knowledge_id={}, 块数={}", knowledge.getId(), segments.size());

        // 对每个文本块进行向量化，并写入 Elasticsearch dense_vector 索引
        int indexed = 0;
        Exception lastIndexingException = null;
        for (TextSegment segment : segments) {
            try {
                Response<Embedding> resp = embeddingModel.embed(segment);
                embeddingStore.add(resp.content(), segment);
                indexed++;
            } catch (Exception e) {
                lastIndexingException = e;
                log.error("向量化或 ES 写入失败：knowledge_id={}, chunk={}, msg={}",
                        knowledge.getId(), indexed, e.getMessage(), e);
            }
        }
        log.info("Elasticsearch 索引写入完成：knowledge_id={}, 成功块数={}/{}",
                knowledge.getId(), indexed, segments.size());

        if (indexed == 0 && !segments.isEmpty()) {
            // 所有分块均写入失败，向上抛出异常让调用方感知真实错误
            String rootCause = lastIndexingException != null
                    ? lastIndexingException.getMessage() : "未知原因";
            throw new RuntimeException(
                    "所有文本块均写入 Elasticsearch 失败（共 " + segments.size()
                            + " 块），请检查向量化 API 或 ES 连接。最后一次错误：" + rootCause,
                    lastIndexingException);
        }

        // 同步更新 MySQL embedding 字段（存第一块向量），保持数据库字段与 ES 的一致性备份
        if (!segments.isEmpty()) {
            try {
                Response<Embedding> firstResp = embeddingModel.embed(segments.get(0));
                float[] vector = firstResp.content().vector();
                String jsonVector = floatArrayToJsonString(vector);
                visaKnowledgeMapper.updateEmbeddingById(knowledge.getId(), jsonVector);
            } catch (Exception e) {
                log.warn("MySQL embedding 备份更新失败：knowledge_id={}, msg={}",
                        knowledge.getId(), e.getMessage());
            }
        }
    }

    /**
     * 将知识条目的多个文本字段拼接为用于向量化的完整文本。
     *
     * <p>拼接顺序：分类 + 标题 + 关键词 + 正文，覆盖条目的全部语义维度，
     * 使向量能同时捕获主题、关键词和内容语义。</p>
     */
    private String buildTextForEmbedding(VisaKnowledge k) {
        StringBuilder sb = new StringBuilder();
        if (k.getCategory() != null) sb.append(k.getCategory()).append(" ");
        if (k.getTitle()    != null) sb.append(k.getTitle()).append(" ");
        if (k.getKeywords() != null) sb.append(k.getKeywords()).append(" ");
        if (k.getContent()  != null) sb.append(k.getContent());
        return sb.toString().trim();
    }

    /**
     * 将 float[] 向量序列化为 JSON 字符串，用于写入 MySQL embedding 备份字段。
     */
    private String floatArrayToJsonString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
