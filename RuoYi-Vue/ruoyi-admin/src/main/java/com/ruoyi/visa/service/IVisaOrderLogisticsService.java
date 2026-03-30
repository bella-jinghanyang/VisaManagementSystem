package com.ruoyi.visa.service;

import java.util.List;
import com.ruoyi.visa.domain.VisaOrderLogistics;

/**
 * 订单物流信息Service接口
 * 
 * @author bella
 * @date 2026-03-29
 */
public interface IVisaOrderLogisticsService 
{
    /**
     * 查询订单物流信息
     * 
     * @param id 订单物流信息主键
     * @return 订单物流信息
     */
    public VisaOrderLogistics selectVisaOrderLogisticsById(Long id);

    /**
     * 查询订单物流信息列表
     * 
     * @param visaOrderLogistics 订单物流信息
     * @return 订单物流信息集合
     */
    public List<VisaOrderLogistics> selectVisaOrderLogisticsList(VisaOrderLogistics visaOrderLogistics);

    /**
     * 新增订单物流信息
     * 
     * @param visaOrderLogistics 订单物流信息
     * @return 结果
     */
    public int insertVisaOrderLogistics(VisaOrderLogistics visaOrderLogistics);

    /**
     * 修改订单物流信息
     * 
     * @param visaOrderLogistics 订单物流信息
     * @return 结果
     */
    public int updateVisaOrderLogistics(VisaOrderLogistics visaOrderLogistics);

    /**
     * 批量删除订单物流信息
     * 
     * @param ids 需要删除的订单物流信息主键集合
     * @return 结果
     */
    public int deleteVisaOrderLogisticsByIds(Long[] ids);

    /**
     * 删除订单物流信息信息
     * 
     * @param id 订单物流信息主键
     * @return 结果
     */
    public int deleteVisaOrderLogisticsById(Long id);
}
