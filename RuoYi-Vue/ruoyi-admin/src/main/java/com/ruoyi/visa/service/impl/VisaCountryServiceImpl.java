package com.ruoyi.visa.service.impl;

import java.util.List;

import com.ruoyi.visa.mapper.VisaProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaCountryMapper;
import com.ruoyi.visa.domain.VisaCountry;
import com.ruoyi.visa.service.IVisaCountryService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 国家配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-01-23
 */
@Service
public class VisaCountryServiceImpl implements IVisaCountryService 
{
    @Autowired
    private VisaCountryMapper visaCountryMapper;

    @Autowired
    private VisaProductMapper visaProductMapper;

    /**
     * 查询国家配置
     * 
     * @param id 国家配置主键
     * @return 国家配置
     */
    @Override
    public VisaCountry selectVisaCountryById(Long id)
    {
        return visaCountryMapper.selectVisaCountryById(id);
    }

    /**
     * 查询国家配置列表
     * 
     * @param visaCountry 国家配置
     * @return 国家配置
     */
    @Override
    public List<VisaCountry> selectVisaCountryList(VisaCountry visaCountry)
    {
        return visaCountryMapper.selectVisaCountryList(visaCountry);
    }

    /**
     * 新增国家配置
     * 
     * @param visaCountry 国家配置
     * @return 结果
     */
    @Override
    public int insertVisaCountry(VisaCountry visaCountry)
    {
        return visaCountryMapper.insertVisaCountry(visaCountry);
    }

    /**
     * 修改国家配置
     * 
     * @param visaCountry 国家配置
     * @return 结果
     */
    @Override
    public int updateVisaCountry(VisaCountry visaCountry)
    {
        return visaCountryMapper.updateVisaCountry(visaCountry);
    }

    /**
     * 批量删除国家配置
     * 
     * @param ids 需要删除的国家配置主键
     * @return 结果
     */
    @Override
    public int deleteVisaCountryByIds(Long[] ids)
    {
        return visaCountryMapper.deleteVisaCountryByIds(ids);
    }

    /**
     * 删除国家配置信息
     * 
     * @param id 国家配置主键
     * @return 结果
     */
    @Override
    public int deleteVisaCountryById(Long id)
    {
        return visaCountryMapper.deleteVisaCountryById(id);
    }

    /**
     * 修改国家状态
     * @param id
     * @param status
     * @return
     */
    @Override
    @Transactional
    public int updateCountryStatus(Long id, String status) {
        // 1. 先更新国家表
        int rows = visaCountryMapper.updateCountryStatus(id, status);

        // 2. 级联逻辑：如果国家被“下架/停用”了 (假设 "0" 代表下架)
        if ("0".equals(status)) {
            // 把该国家下的所有产品状态改为 0 (下架)
            visaProductMapper.updateProductStatusByCountryId(id, 0);
        }

        return rows;
    }


}
