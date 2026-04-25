-- ============================================================
-- RAG 架构升级迁移脚本
-- 版本：v2.0.0-rag-upgrade
-- 说明：新增 source_file 字段，用于记录管理员通过 /upload-doc
--       接口上传到 MinIO 的原始文档路径（object name）。
--       手动录入的文本型知识条目此字段为 NULL。
-- ============================================================

ALTER TABLE visa_knowledge
    ADD COLUMN source_file VARCHAR(500) NULL COMMENT 'MinIO 原始文档路径（object name），仅文件上传时填充'
    AFTER embedding;
