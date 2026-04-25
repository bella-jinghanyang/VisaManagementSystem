package com.ruoyi.visa.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置类
 *
 * <p>MinIO 是兼容 AWS S3 协议的开源对象存储服务，本系统以其替代 MySQL BLOB 字段，
 * 专用于持久化知识库管理员上传的签证政策原始文档（PDF / Word / Excel 等）。</p>
 *
 * <p>启动时自动检查并创建 {@code visa-knowledge-docs} Bucket，确保文件上传流程
 * 无须手动预建存储桶。</p>
 *
 * <p>本地开发启动命令（Docker）：<br>
 * {@code docker run -d -p 9000:9000 -p 9001:9001 minio/minio server /data --console-address ":9001"}<br>
 * 控制台地址：http://localhost:9001，默认账号/密码均为 {@code minioadmin}。</p>
 *
 * @author bella
 */
@Configuration
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * MinIO 客户端 Bean。
     *
     * <p>初始化时自动检测目标 Bucket 是否存在；若不存在则自动创建，
     * 保证服务首次启动即可正常写入文件，无须人工干预。</p>
     */
    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        ensureBucketExists(client);
        return client;
    }

    /**
     * 检查并按需创建文档存储桶。
     *
     * @param client 已构建的 MinIO 客户端实例
     */
    private void ensureBucketExists(MinioClient client) {
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO Bucket 已自动创建：{}", bucketName);
            } else {
                log.info("MinIO Bucket 已存在：{}", bucketName);
            }
        } catch (Exception e) {
            log.warn("MinIO Bucket 检查失败（服务可能未启动），跳过创建：{}", e.getMessage());
        }
    }
}
