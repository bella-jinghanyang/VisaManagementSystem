package com.ruoyi.visa.service;

import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件存储服务
 *
 * <p>封装 MinIO Java SDK 的核心操作，为知识库文档提供：</p>
 * <ul>
 *   <li>文件上传：将管理员提交的 PDF/Word 原始文件持久化到 MinIO 对象存储；</li>
 *   <li>文件流获取：供 Apache Tika 读取原始字节流进行文本提取；</li>
 *   <li>预签名 URL 生成：供前端直接下载原始文件，无需经过 Spring Boot 中转，减轻服务器压力。</li>
 * </ul>
 *
 * <p>MinIO 兼容 AWS S3 协议，存储路径格式为：
 * {@code {bucketName}/{yyyy-MM}/{uuid}.{ext}}，
 * 按月份分目录避免单目录文件过多导致的性能下降。</p>
 *
 * @author bella
 */
@Service
public class MinioService {

    private static final Logger log = LoggerFactory.getLogger(MinioService.class);

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 上传文件到 MinIO，返回对象路径（即 object name）。
     *
     * <p>对象路径格式：{@code {originalFilename}_{uuid}.{ext}}，
     * 确保重名文件不会相互覆盖，同时保留原始文件名便于管理员识别。</p>
     *
     * @param file 管理员上传的 MultipartFile（PDF/Word/Excel 等）
     * @return MinIO 对象路径，写入 {@code visa_knowledge.source_file} 字段
     * @throws RuntimeException 若上传失败则包装为运行时异常向上抛出
     */
    public String uploadFile(MultipartFile file) {
        String originalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "unknown";
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";
        String baseName = originalName.contains(".")
                ? originalName.substring(0, originalName.lastIndexOf('.'))
                : originalName;
        String objectName = baseName + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            log.info("文件已上传至 MinIO：bucket={}, object={}, size={}bytes",
                    bucketName, objectName, file.getSize());
            return objectName;
        } catch (Exception e) {
            log.error("MinIO 文件上传失败：{}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取指定对象的输入流，供 Apache Tika 解析文件内容。
     *
     * <p>调用方负责关闭返回的 {@link InputStream}，建议使用 try-with-resources。</p>
     *
     * @param objectName MinIO 对象路径（即 {@code uploadFile} 的返回值）
     * @return 文件字节流
     */
    public InputStream getFileStream(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            log.error("MinIO 文件获取失败：object={}, msg={}", objectName, e.getMessage(), e);
            throw new RuntimeException("文件读取失败：" + e.getMessage(), e);
        }
    }

    /**
     * 生成带签名的临时下载 URL（有效期 1 小时），供前端直连 MinIO 下载原始文件。
     *
     * <p>预签名 URL 包含访问凭证，无需二次鉴权，适合后台管理员下载核对原文的场景。</p>
     *
     * @param objectName MinIO 对象路径
     * @return 带签名的 HTTPS URL；生成失败时返回空字符串
     */
    public String generatePresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            log.error("MinIO 预签名 URL 生成失败：object={}, msg={}", objectName, e.getMessage(), e);
            return "";
        }
    }
}
