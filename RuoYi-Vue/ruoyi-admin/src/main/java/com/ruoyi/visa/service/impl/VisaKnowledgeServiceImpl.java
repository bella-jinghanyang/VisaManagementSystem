package com.ruoyi.visa.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    @Autowired
    private VisaKnowledgeMapper visaKnowledgeMapper;

    /**
     * 查询签证知识库
     * 
     * @param id 签证知识库主键
     * @return 签证知识库
     */
    @Override
    public VisaKnowledge selectVisaKnowledgeById(Long id)
    {
        return visaKnowledgeMapper.selectVisaKnowledgeById(id);
    }

    /**
     * 查询签证知识库列表
     * 
     * @param visaKnowledge 签证知识库
     * @return 签证知识库
     */
    @Override
    public List<VisaKnowledge> selectVisaKnowledgeList(VisaKnowledge visaKnowledge)
    {

        return visaKnowledgeMapper.selectVisaKnowledgeList(visaKnowledge);
    }

    /**
     * 新增签证知识库
     * 
     * @param visaKnowledge 签证知识库
     * @return 结果
     */
    @Override
    public int insertVisaKnowledge(VisaKnowledge visaKnowledge)
    {
        visaKnowledge.setCreateTime(DateUtils.getNowDate());
        return visaKnowledgeMapper.insertVisaKnowledge(visaKnowledge);
    }

    /**
     * 修改签证知识库
     * 
     * @param visaKnowledge 签证知识库
     * @return 结果
     */
    @Override
    public int updateVisaKnowledge(VisaKnowledge visaKnowledge)
    {
        return visaKnowledgeMapper.updateVisaKnowledge(visaKnowledge);
    }

    /**
     * 批量删除签证知识库
     * 
     * @param ids 需要删除的签证知识库主键
     * @return 结果
     */
    @Override
    public int deleteVisaKnowledgeByIds(Long[] ids)
    {
        return visaKnowledgeMapper.deleteVisaKnowledgeByIds(ids);
    }

    /**
     * 删除签证知识库信息
     * 
     * @param id 签证知识库主键
     * @return 结果
     */
    @Override
    public int deleteVisaKnowledgeById(Long id)
    {
        return visaKnowledgeMapper.deleteVisaKnowledgeById(id);
    }
}
