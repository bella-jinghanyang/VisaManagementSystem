package com.ruoyi.visa.mapper;

import java.util.List;
import com.ruoyi.visa.domain.VisaKnowledge;

/**
 * 签证知识库Mapper接口
 * 
 * @author bella
 * @date 2026-02-17
 */
public interface VisaKnowledgeMapper 
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
     * 删除签证知识库
     * 
     * @param id 签证知识库主键
     * @return 结果
     */
    public int deleteVisaKnowledgeById(Long id);

    /**
     * 批量删除签证知识库
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteVisaKnowledgeByIds(Long[] ids);
}
