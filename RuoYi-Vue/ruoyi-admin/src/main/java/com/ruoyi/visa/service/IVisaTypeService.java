package com.ruoyi.visa.service;

import java.util.List;
import com.ruoyi.visa.domain.VisaType;

/**
 * 签证类型Service接口
 * 
 * @author bella
 * @date 2026-01-23
 */
public interface IVisaTypeService 
{
    /**
     * 查询签证类型
     * 
     * @param id 签证类型主键
     * @return 签证类型
     */
    public VisaType selectVisaTypeById(Long id);

    /**
     * 查询签证类型列表
     * 
     * @param visaType 签证类型
     * @return 签证类型集合
     */
    public List<VisaType> selectVisaTypeList(VisaType visaType);

    /**
     * 新增签证类型
     * 
     * @param visaType 签证类型
     * @return 结果
     */
    public int insertVisaType(VisaType visaType);

    /**
     * 修改签证类型
     * 
     * @param visaType 签证类型
     * @return 结果
     */
    public int updateVisaType(VisaType visaType);

    /**
     * 批量删除签证类型
     * 
     * @param ids 需要删除的签证类型主键集合
     * @return 结果
     */
    public int deleteVisaTypeByIds(Long[] ids);

    /**
     * 删除签证类型信息
     * 
     * @param id 签证类型主键
     * @return 结果
     */
    public int deleteVisaTypeById(Long id);

    /**
     * 修改签证类型状态
     * @param id
     * @param status
     * @return
     */
    int updateTypeStatus(Long id, String status);
}
