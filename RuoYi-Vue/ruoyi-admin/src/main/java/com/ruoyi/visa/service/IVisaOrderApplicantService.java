package com.ruoyi.visa.service;

import java.util.List;
import com.ruoyi.visa.domain.VisaOrderApplicant;

/**
 * 订单申请人基本信息Service接口
 * 
 * @author bella
 * @date 2026-03-28
 */
public interface IVisaOrderApplicantService 
{
    /**
     * 查询订单申请人基本信息
     * 
     * @param id 订单申请人基本信息主键
     * @return 订单申请人基本信息
     */
    public VisaOrderApplicant selectVisaOrderApplicantById(Long id);

    /**
     * 查询订单申请人基本信息列表
     * 
     * @param visaOrderApplicant 订单申请人基本信息
     * @return 订单申请人基本信息集合
     */
    public List<VisaOrderApplicant> selectVisaOrderApplicantList(VisaOrderApplicant visaOrderApplicant);

    /**
     * 新增订单申请人基本信息
     * 
     * @param visaOrderApplicant 订单申请人基本信息
     * @return 结果
     */
    public int insertVisaOrderApplicant(VisaOrderApplicant visaOrderApplicant);

    /**
     * 修改订单申请人基本信息
     * 
     * @param visaOrderApplicant 订单申请人基本信息
     * @return 结果
     */
    public int updateVisaOrderApplicant(VisaOrderApplicant visaOrderApplicant);

    /**
     * 批量删除订单申请人基本信息
     * 
     * @param ids 需要删除的订单申请人基本信息主键集合
     * @return 结果
     */
    public int deleteVisaOrderApplicantByIds(Long[] ids);

    /**
     * 删除订单申请人基本信息信息
     * 
     * @param id 订单申请人基本信息主键
     * @return 结果
     */
    public int deleteVisaOrderApplicantById(Long id);
}
