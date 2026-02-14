package com.ruoyi.visa.mapper;

import java.util.List;
import com.ruoyi.visa.domain.VisaCountry;
import org.apache.ibatis.annotations.Param;

/**
 * 国家配置Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-23
 */
public interface VisaCountryMapper 
{
    /**
     * 查询国家配置
     * 
     * @param id 国家配置主键
     * @return 国家配置
     */
    public VisaCountry selectVisaCountryById(Long id);

    /**
     * 查询国家配置列表
     * 
     * @param visaCountry 国家配置
     * @return 国家配置集合
     */
    public List<VisaCountry> selectVisaCountryList(VisaCountry visaCountry);

    /**
     * 新增国家配置
     * 
     * @param visaCountry 国家配置
     * @return 结果
     */
    public int insertVisaCountry(VisaCountry visaCountry);

    /**
     * 修改国家配置
     * 
     * @param visaCountry 国家配置
     * @return 结果
     */
    public int updateVisaCountry(VisaCountry visaCountry);

    /**
     * 删除国家配置
     * 
     * @param id 国家配置主键
     * @return 结果
     */
    public int deleteVisaCountryById(Long id);

    /**
     * 批量删除国家配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteVisaCountryByIds(Long[] ids);

    /**
     * 修改国家状态
     * @param id
     * @param status
     * @return
     */
    int updateCountryStatus(@Param("id") Long id, @Param("status") String status);
}
