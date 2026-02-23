package com.ruoyi.visa.service;

import java.util.List;
import com.ruoyi.visa.domain.VisaComment;

/**
 * 签证评价Service接口
 * 
 * @author bella
 * @date 2026-02-20
 */
public interface IVisaCommentService 
{
    /**
     * 查询签证评价
     * 
     * @param id 签证评价主键
     * @return 签证评价
     */
    public VisaComment selectVisaCommentById(Long id);

    /**
     * 查询签证评价列表
     * 
     * @param visaComment 签证评价
     * @return 签证评价集合
     */
    public List<VisaComment> selectVisaCommentList(VisaComment visaComment);

    /**
     * 新增签证评价
     * 
     * @param visaComment 签证评价
     * @return 结果
     */
    public int insertVisaComment(VisaComment visaComment);

    /**
     * 修改签证评价
     * 
     * @param visaComment 签证评价
     * @return 结果
     */
    public int updateVisaComment(VisaComment visaComment);

    /**
     * 批量删除签证评价
     * 
     * @param ids 需要删除的签证评价主键集合
     * @return 结果
     */
    public int deleteVisaCommentByIds(Long[] ids);

    /**
     * 删除签证评价信息
     * 
     * @param id 签证评价主键
     * @return 结果
     */
    public int deleteVisaCommentById(Long id);
}
