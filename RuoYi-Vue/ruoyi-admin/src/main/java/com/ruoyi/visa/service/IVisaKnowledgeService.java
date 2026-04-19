package com.ruoyi.visa.service;

import java.util.List;
import com.ruoyi.visa.domain.VisaKnowledge;

/**
 * 签证知识库Service接口
 * 
 * @author bella
 * @date 2026-02-17
 */
public interface IVisaKnowledgeService 
{
    /**
     * 查询签证知识库
     * 
     * @param id 签证知识库主键
     * @return 签证知识库
     */
    public VisaKnowledge selectVisaKnowledgeById(Long id);

    /**
     * 查询签证知识库列表
     * 
     * @param visaKnowledge 签证知识库
     * @return 签证知识库集合
     */
    public List<VisaKnowledge> selectVisaKnowledgeList(VisaKnowledge visaKnowledge);

    /**
     * 新增签证知识库
     * 
     * @param visaKnowledge 签证知识库
     * @return 结果
     */
    public int insertVisaKnowledge(VisaKnowledge visaKnowledge);

    /**
     * 修改签证知识库
     * 
     * @param visaKnowledge 签证知识库
     * @return 结果
     */
    public int updateVisaKnowledge(VisaKnowledge visaKnowledge);

    /**
     * 批量删除签证知识库
     * 
     * @param ids 需要删除的签证知识库主键集合
     * @return 结果
     */
    public int deleteVisaKnowledgeByIds(Long[] ids);

    /**
     * 删除签证知识库信息
     * 
     * @param id 签证知识库主键
     * @return 结果
     */
    public int deleteVisaKnowledgeById(Long id);

    /**
     * 查询全部有效知识条目（含向量字段），供 RAG 检索使用。
     */
    public List<VisaKnowledge> selectAllActiveWithEmbedding();

    /**
     * 批量（重新）生成所有有效知识条目的语义向量。
     * 适用于：首次上线、更换 Embedding 模型、批量导入数据后的初始化。
     *
     * @return 成功更新的条目数
     */
    public int refreshAllEmbeddings();
}
