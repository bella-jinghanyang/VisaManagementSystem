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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

        // 步骤 ②：通过 Apache Tika 从文件流中提取纯文本。
        // 写入临时文件后再解析，使 POI 改走 JDK 内置的 java.util.zip.ZipFile（随机访问）
        // 而非 commons-compress 的 ZipArchiveInputStream（依赖 ServiceLoader），
        // 从根本上规避 fat jar 打包时 META-INF/services 文件被覆盖导致的
        // "No Archiver found for the stream signature" 问题。
        String rawText;
        try (InputStream raw = minioService.getFileStream(objectName)) {
            rawText = parseWithTempFile(raw, objectName);

            // 将 Tika 解析出的正文持久化至 MySQL content 字段，
            // 确保后续 refreshAllEmbeddings / 管理员预览时均可读取完整原文，
            // 而不是只有 title/keywords 等元数据。
            if (rawText != null && !rawText.trim().isEmpty()) {
                knowledge.setContent(rawText);
                visaKnowledgeMapper.updateContentById(knowledge.getId(), rawText);
                log.info("文档原文已持久化至 MySQL content：knowledge_id={}, 字符数={}",
                        knowledge.getId(), rawText.length());
            }
        } catch (Exception e) {
            // 当 Tika 解析失败且 MySQL content 字段为空时，无可用正文内容可向量化，
            // 直接返回 MinIO 路径并记录错误；若 MySQL 已存有历史正文（如管理员曾手动录入），
            // 则降级使用已有 content 继续摄取，避免服务中断。
            if (knowledge.getContent() == null || knowledge.getContent().trim().isEmpty()) {
                log.error("Tika 解析文件失败且 content 为空，跳过向量化：" +
                        "knowledge_id={}, object={}, error={}", knowledge.getId(), objectName, e.getMessage());
                return objectName;
            }
            log.warn("Tika 解析文件失败，降级使用已有 content 字段继续摄取：" +
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
     * <p>若条目的 {@code content} 字段为空，但 {@code source_file} 字段指向 MinIO 中的
     * 已存储文件（通常是首次上传时未能持久化 Tika 结果的历史数据），则自动从 MinIO 重新
     * 下载并解析原始文档，同时将解析结果回写至 MySQL {@code content} 字段，
     * 确保后续刷新无需再次访问 MinIO。</p>
     *
     * @param knowledge 包含 title / category / keywords / content / source_file 的知识条目
     */
    @Async
    public void ingestTextAsync(VisaKnowledge knowledge) {
        try {
            // 若 content 为空但 source_file 存在，从 MinIO 重新解析原始文档并持久化
            if ((knowledge.getContent() == null || knowledge.getContent().trim().isEmpty())
                    && knowledge.getSourceFile() != null && !knowledge.getSourceFile().trim().isEmpty()) {
                log.info("content 为空但 source_file 存在，从 MinIO 重新解析：knowledge_id={}, object={}",
                        knowledge.getId(), knowledge.getSourceFile());
                String reparsed = parseFromMinio(knowledge.getSourceFile());
                if (reparsed != null && !reparsed.trim().isEmpty()) {
                    knowledge.setContent(reparsed);
                    visaKnowledgeMapper.updateContentById(knowledge.getId(), reparsed);
                    log.info("MinIO 重新解析完成，已持久化至 MySQL：knowledge_id={}, 字符数={}",
                            knowledge.getId(), reparsed.length());
                }
            }

            String text = buildTextForEmbedding(knowledge);
            if (text.isEmpty()) {
                log.warn("知识条目文本为空，跳过向量化：knowledge_id={}", knowledge.getId());
                return;
            }
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
     * 从 MinIO 下载原始文档并通过 Apache Tika 提取纯文本。
     *
     * <p>用于在 {@code content} 字段为空时，对历史数据进行补偿解析。
     * 方法内部捕获所有异常并以 {@code null} 返回，调用方负责降级处理。</p>
     *
     * @param objectName MinIO 对象路径（来自 {@code visa_knowledge.source_file}）
     * @return 提取的纯文本；解析失败或结果为空时返回 {@code null}
     */
    private String parseFromMinio(String objectName) {
        try (InputStream raw = minioService.getFileStream(objectName)) {
            String text = parseWithTempFile(raw, objectName);
            return (text != null && !text.trim().isEmpty()) ? text : null;
        } catch (Exception e) {
            log.warn("从 MinIO 重新解析文件失败，跳过补偿：object={}, error={}", objectName, e.getMessage());
            return null;
        }
    }

    /**
     * 将 MinIO 流写入临时文件后由 Apache Tika 解析，规避 fat jar 环境下
     * commons-compress ServiceLoader 被覆盖导致的 "No Archiver found" 问题。
     *
     * <p>当 Tika 通过 {@code InputStream} 解析 .docx/.xlsx 等 Office Open XML 格式时，
     * 底层 Apache POI 使用 commons-compress 的 {@code ZipArchiveInputStream}，
     * 该类通过 Java ServiceLoader 加载 {@code ArchiveStreamProvider}。
     * Spring Boot fat jar 打包时多个 jar 的同名 META-INF/services 文件相互覆盖，
     * 导致 ServiceLoader 找不到 {@code ZipArchiveStreamProvider}，进而抛出
     * "No Archiver found for the stream signature"。</p>
     *
     * <p>通过临时文件路径解析时，POI 改用 JDK 内置的 {@code java.util.zip.ZipFile}
     * 进行随机访问读取，完全绕开 commons-compress 的 ServiceLoader 依赖。
     * 文件扩展名从 objectName 中提取，确保 Tika 的格式探测结果正确。</p>
     *
     * @param inputStream MinIO 返回的原始流（调用方负责关闭）
     * @param objectName  MinIO 对象名（用于提取文件扩展名）
     * @return Tika 提取的纯文本
     * @throws Exception IO 异常或 Tika 内部解析异常
     */
    private String parseWithTempFile(InputStream inputStream, String objectName) throws Exception {
        int dotIdx = objectName.lastIndexOf('.');
        String suffix = dotIdx >= 0 ? objectName.substring(dotIdx) : ".tmp";
        Path tempFile = Files.createTempFile("tika-ingest-", suffix);
        try {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // 【关键改动】使用更稳定的解析方式，显式传入 Metadata 帮助 Tika 识别格式
            org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
            metadata.set(org.apache.tika.metadata.TikaCoreProperties.RESOURCE_NAME_KEY, objectName);

            // 使用单例 Tika 实例解析文件
            return TIKA.parseToString(tempFile.toFile());
        } finally {
            Files.deleteIfExists(tempFile);
        }
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
