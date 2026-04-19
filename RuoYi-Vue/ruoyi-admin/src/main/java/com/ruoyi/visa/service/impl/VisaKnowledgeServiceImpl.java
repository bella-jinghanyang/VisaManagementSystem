package com.ruoyi.visa.service.impl;

import java.util.List;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.visa.service.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.visa.mapper.VisaKnowledgeMapper;
import com.ruoyi.visa.domain.VisaKnowledge;
import com.ruoyi.visa.service.IVisaKnowledgeService;

/**
 * 签证知识库Service业务层处理
 *
 * @author bella
 * @date 2026-02-17
 */
@Service
public class VisaKnowledgeServiceImpl implements IVisaKnowledgeService
{
    private static final Logger log = LoggerFactory.getLogger(VisaKnowledgeServiceImpl.class);

    @Autowired
    private VisaKnowledgeMapper visaKnowledgeMapper;

    @Autowired
    private EmbeddingService embeddingService;

    @Override
    public VisaKnowledge selectVisaKnowledgeById(Long id)
    {
        return visaKnowledgeMapper.selectVisaKnowledgeById(id);
    }

    @Override
    public List<VisaKnowledge> selectVisaKnowledgeList(VisaKnowledge visaKnowledge)
    {
        return visaKnowledgeMapper.selectVisaKnowledgeList(visaKnowledge);
    }

    @Override
    public List<VisaKnowledge> selectAllActiveWithEmbedding()
    {
        return visaKnowledgeMapper.selectAllActiveWithEmbedding();
    }

    /**
     * 新增知识条目，并异步生成其语义向量。
     * 异步化处理确保接口响应不受 Embedding API 网络延迟影响。
     */
    @Override
    public int insertVisaKnowledge(VisaKnowledge visaKnowledge)
    {
        visaKnowledge.setCreateTime(DateUtils.getNowDate());
        int rows = visaKnowledgeMapper.insertVisaKnowledge(visaKnowledge);
        if (rows > 0 && visaKnowledge.getId() != null) {
            asyncGenerateAndSaveEmbedding(visaKnowledge.getId(), visaKnowledge);
        }

        return rows;
    }

    /**
     * 修改知识条目，并异步重新生成其语义向量，保持向量与内容的一致性。
     */
    @Override
    public int updateVisaKnowledge(VisaKnowledge visaKnowledge)
    {
        int rows = visaKnowledgeMapper.updateVisaKnowledge(visaKnowledge);
        if (rows > 0) {
            // 重新查一次完整对象，确保用于向量化的文本是最新内容
            VisaKnowledge latest = visaKnowledgeMapper.selectVisaKnowledgeById(visaKnowledge.getId());
            if (latest != null) {
                asyncGenerateAndSaveEmbedding(latest.getId(), latest);
            }
        }
        return rows;
    }

    @Override
    public int deleteVisaKnowledgeByIds(Long[] ids)
    {
        return visaKnowledgeMapper.deleteVisaKnowledgeByIds(ids);
    }

    @Override
    public int deleteVisaKnowledgeById(Long id)
    {
        return visaKnowledgeMapper.deleteVisaKnowledgeById(id);
    }

    /**
     * 批量刷新所有有效知识条目的语义向量。
     * 适用于首次上线或更换 Embedding 模型后的初始化操作。
     */
    @Anonymous
    @Override
    public int refreshAllEmbeddings()
    {
        List<VisaKnowledge> list = visaKnowledgeMapper.selectAllActiveWithEmbedding();
        int count = 0;
        for (VisaKnowledge item : list) {
            String text = buildTextForEmbedding(item);
            List<Double> vector = embeddingService.embed(text);
            if (!vector.isEmpty()) {
                String json = embeddingService.vectorToJson(vector);
                visaKnowledgeMapper.updateEmbeddingById(item.getId(), json);
                count++;
                log.info("已生成向量：id={}, title={}", item.getId(), item.getTitle());
            }
        }
        log.info("知识库向量刷新完成，共更新 {} 条", count);
        return count;
    }

    // =========================================================
    //  私有方法
    // =========================================================

    /**
     * 异步生成并持久化向量，避免阻塞主流程。
     * 需要在 Spring Boot 启动类上加 @EnableAsync 才能生效。
     */
    @Async
    public void asyncGenerateAndSaveEmbedding(Long id, VisaKnowledge knowledge) {
        try {
            String text = buildTextForEmbedding(knowledge);
            List<Double> vector = embeddingService.embed(text);
            if (!vector.isEmpty()) {
                String json = embeddingService.vectorToJson(vector);
                visaKnowledgeMapper.updateEmbeddingById(id, json);
                log.info("向量生成成功：id={}", id);
            }
        } catch (Exception e) {
            log.error("向量生成异常：id={}, msg={}", id, e.getMessage());
        }
    }

    /**
     * 将知识条目的多个字段拼接为用于向量化的完整文本。
     * 拼接策略：类别 + 标题 + 关键词 + 正文，涵盖条目的全部语义信息。
     */
    private String buildTextForEmbedding(VisaKnowledge k) {
        StringBuilder sb = new StringBuilder();
        if (k.getCategory() != null) sb.append(k.getCategory()).append(" ");
        if (k.getTitle()    != null) sb.append(k.getTitle()).append(" ");
        if (k.getKeywords() != null) sb.append(k.getKeywords()).append(" ");
        if (k.getContent()  != null) sb.append(k.getContent());
        return sb.toString().trim();
    }


}

