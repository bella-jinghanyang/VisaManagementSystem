package com.ruoyi.visa.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaOrderApplicantMapper;
import com.ruoyi.visa.domain.VisaOrderApplicant;
import com.ruoyi.visa.service.IVisaOrderApplicantService;

/**
 * 订单申请人基本信息Service业务层处理
 * 
 * @author bella
 * @date 2026-03-28
 */
@Service
public class VisaOrderApplicantServiceImpl implements IVisaOrderApplicantService 
{
    @Autowired
    private VisaOrderApplicantMapper visaOrderApplicantMapper;

    /**
     * 查询订单申请人基本信息
     * 
     * @param id 订单申请人基本信息主键
     * @return 订单申请人基本信息
     */
    @Override
    public VisaOrderApplicant selectVisaOrderApplicantById(Long id)
    {
        return visaOrderApplicantMapper.selectVisaOrderApplicantById(id);
    }

    /**
     * 查询订单申请人基本信息列表
     * 
     * @param visaOrderApplicant 订单申请人基本信息
     * @return 订单申请人基本信息
     */
    @Override
    public List<VisaOrderApplicant> selectVisaOrderApplicantList(VisaOrderApplicant visaOrderApplicant)
    {
        return visaOrderApplicantMapper.selectVisaOrderApplicantList(visaOrderApplicant);
    }

    /**
     * 新增订单申请人基本信息
     * 
     * @param visaOrderApplicant 订单申请人基本信息
     * @return 结果
     */
    @Override
    public int insertVisaOrderApplicant(VisaOrderApplicant visaOrderApplicant)
    {
        visaOrderApplicant.setCreateTime(DateUtils.getNowDate());
        return visaOrderApplicantMapper.insertVisaOrderApplicant(visaOrderApplicant);
    }

    /**
     * 修改订单申请人基本信息
     * 
     * @param visaOrderApplicant 订单申请人基本信息
     * @return 结果
     */
    @Override
    public int updateVisaOrderApplicant(VisaOrderApplicant visaOrderApplicant)
    {
        return visaOrderApplicantMapper.updateVisaOrderApplicant(visaOrderApplicant);
    }

    /**
     * 批量删除订单申请人基本信息
     * 
     * @param ids 需要删除的订单申请人基本信息主键
     * @return 结果
     */
    @Override
    public int deleteVisaOrderApplicantByIds(Long[] ids)
    {
        return visaOrderApplicantMapper.deleteVisaOrderApplicantByIds(ids);
    }

    /**
     * 删除订单申请人基本信息信息
     * 
     * @param id 订单申请人基本信息主键
     * @return 结果
     */
    @Override
    public int deleteVisaOrderApplicantById(Long id)
    {
        return visaOrderApplicantMapper.deleteVisaOrderApplicantById(id);
    }
}
