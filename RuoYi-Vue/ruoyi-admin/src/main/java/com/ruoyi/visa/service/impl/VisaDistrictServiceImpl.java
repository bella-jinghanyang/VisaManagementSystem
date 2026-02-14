package com.ruoyi.visa.service.impl;

import java.util.List;

import com.ruoyi.visa.mapper.VisaProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaDistrictMapper;
import com.ruoyi.visa.domain.VisaDistrict;
import com.ruoyi.visa.service.IVisaDistrictService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 领区配置Service业务层处理
 * 
 * @author bella
 * @date 2026-01-23
 */
@Service
public class VisaDistrictServiceImpl implements IVisaDistrictService 
{
    @Autowired
    private VisaDistrictMapper visaDistrictMapper;

    @Autowired
    private VisaProductMapper visaProductMapper;

    /**
     * 查询领区配置
     * 
     * @param id 领区配置主键
     * @return 领区配置
     */
    @Override
    public VisaDistrict selectVisaDistrictById(Long id)
    {
        return visaDistrictMapper.selectVisaDistrictById(id);
    }

    /**
     * 查询领区配置列表
     * 
     * @param visaDistrict 领区配置
     * @return 领区配置
     */
    @Override
    public List<VisaDistrict> selectVisaDistrictList(VisaDistrict visaDistrict)
    {
        return visaDistrictMapper.selectVisaDistrictList(visaDistrict);
    }

    /**
     * 新增领区配置
     * 
     * @param visaDistrict 领区配置
     * @return 结果
     */
    @Override
    public int insertVisaDistrict(VisaDistrict visaDistrict)
    {
        return visaDistrictMapper.insertVisaDistrict(visaDistrict);
    }

    /**
     * 修改领区配置
     * 
     * @param visaDistrict 领区配置
     * @return 结果
     */
    @Override
    public int updateVisaDistrict(VisaDistrict visaDistrict)
    {
        return visaDistrictMapper.updateVisaDistrict(visaDistrict);
    }

    /**
     * 批量删除领区配置
     * 
     * @param ids 需要删除的领区配置主键
     * @return 结果
     */
    @Override
    public int deleteVisaDistrictByIds(Long[] ids)
    {
        return visaDistrictMapper.deleteVisaDistrictByIds(ids);
    }

    /**
     * 删除领区配置信息
     * 
     * @param id 领区配置主键
     * @return 结果
     */
    @Override
    public int deleteVisaDistrictById(Long id)
    {
        return visaDistrictMapper.deleteVisaDistrictById(id);
    }

    /**
     * 修改领区状态
     * @param id
     * @param status
     * @return
     */
    @Override
    @Transactional
    public int updateDistrictStatus(Long id, String status) {
        // 1. 先更新领区表
        int rows = visaDistrictMapper.updateDistrictStatus(id, status);

        // 2. 级联逻辑：如果领区被“下架/停用”了 (假设 "0" 代表下架)
        if ("0".equals(status)) {
            // 把该领区下的所有产品状态改为 0 (下架)
            visaProductMapper.updateProductStatusByDistrictId(id, 0);
        }

        return rows;
    }

}
