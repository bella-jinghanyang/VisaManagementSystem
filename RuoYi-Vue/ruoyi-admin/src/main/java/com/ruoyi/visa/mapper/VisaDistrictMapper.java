package com.ruoyi.visa.mapper;

import java.util.List;
import com.ruoyi.visa.domain.VisaDistrict;
import org.apache.ibatis.annotations.Param;

/**
 * 领区配置Mapper接口
 * 
 * @author bella
 * @date 2026-01-23
 */
public interface VisaDistrictMapper 
{
    /**
     * 查询领区配置
     * 
     * @param id 领区配置主键
     * @return 领区配置
     */
    public VisaDistrict selectVisaDistrictById(Long id);

    /**
     * 查询领区配置列表
     * 
     * @param visaDistrict 领区配置
     * @return 领区配置集合
     */
    public List<VisaDistrict> selectVisaDistrictList(VisaDistrict visaDistrict);

    /**
     * 新增领区配置
     * 
     * @param visaDistrict 领区配置
     * @return 结果
     */
    public int insertVisaDistrict(VisaDistrict visaDistrict);

    /**
     * 修改领区配置
     * 
     * @param visaDistrict 领区配置
     * @return 结果
     */
    public int updateVisaDistrict(VisaDistrict visaDistrict);

    /**
     * 删除领区配置
     * 
     * @param id 领区配置主键
     * @return 结果
     */
    public int deleteVisaDistrictById(Long id);

    /**
     * 批量删除领区配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteVisaDistrictByIds(Long[] ids);

    /**
     * 修改领区状态
     * @param id
     * @param status
     * @return
     */
    int updateDistrictStatus(@Param("id") Long id, @Param("status") String status);
}
