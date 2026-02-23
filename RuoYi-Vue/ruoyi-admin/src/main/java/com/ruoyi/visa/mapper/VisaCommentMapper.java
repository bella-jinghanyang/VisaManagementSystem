package com.ruoyi.visa.mapper;

import java.util.List;
import com.ruoyi.visa.domain.VisaComment;

/**
 * 签证评价Mapper接口
 * 
 * @author bella
 * @date 2026-02-20
 */
public interface VisaCommentMapper 
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
     * 删除签证评价
     * 
     * @param id 签证评价主键
     * @return 结果
     */
    public int deleteVisaCommentById(Long id);

    /**
     * 批量删除签证评价
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteVisaCommentByIds(Long[] ids);
}
