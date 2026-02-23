package com.ruoyi.visa.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaCommentMapper;
import com.ruoyi.visa.domain.VisaComment;
import com.ruoyi.visa.service.IVisaCommentService;

/**
 * 签证评价Service业务层处理
 * 
 * @author bella
 * @date 2026-02-20
 */
@Service
public class VisaCommentServiceImpl implements IVisaCommentService 
{
    @Autowired
    private VisaCommentMapper visaCommentMapper;

    /**
     * 查询签证评价
     * 
     * @param id 签证评价主键
     * @return 签证评价
     */
    @Override
    public VisaComment selectVisaCommentById(Long id)
    {
        return visaCommentMapper.selectVisaCommentById(id);
    }

    /**
     * 查询签证评价列表
     * 
     * @param visaComment 签证评价
     * @return 签证评价
     */
    @Override
    public List<VisaComment> selectVisaCommentList(VisaComment visaComment)
    {
        return visaCommentMapper.selectVisaCommentList(visaComment);
    }

    /**
     * 新增签证评价
     * 
     * @param visaComment 签证评价
     * @return 结果
     */
    @Override
    public int insertVisaComment(VisaComment visaComment)
    {
        visaComment.setCreateTime(DateUtils.getNowDate());
        return visaCommentMapper.insertVisaComment(visaComment);
    }

    /**
     * 修改签证评价
     * 
     * @param visaComment 签证评价
     * @return 结果
     */
    @Override
    public int updateVisaComment(VisaComment visaComment)
    {
        return visaCommentMapper.updateVisaComment(visaComment);
    }

    /**
     * 批量删除签证评价
     * 
     * @param ids 需要删除的签证评价主键
     * @return 结果
     */
    @Override
    public int deleteVisaCommentByIds(Long[] ids)
    {
        return visaCommentMapper.deleteVisaCommentByIds(ids);
    }

    /**
     * 删除签证评价信息
     * 
     * @param id 签证评价主键
     * @return 结果
     */
    @Override
    public int deleteVisaCommentById(Long id)
    {
        return visaCommentMapper.deleteVisaCommentById(id);
    }
}
