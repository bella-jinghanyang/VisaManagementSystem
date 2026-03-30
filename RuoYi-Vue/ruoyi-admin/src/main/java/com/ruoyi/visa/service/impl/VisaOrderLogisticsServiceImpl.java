package com.ruoyi.visa.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaOrderLogisticsMapper;
import com.ruoyi.visa.domain.VisaOrderLogistics;
import com.ruoyi.visa.service.IVisaOrderLogisticsService;

/**
 * 订单物流信息Service业务层处理
 * 
 * @author bella
 * @date 2026-03-29
 */
@Service
public class VisaOrderLogisticsServiceImpl implements IVisaOrderLogisticsService 
{
    @Autowired
    private VisaOrderLogisticsMapper visaOrderLogisticsMapper;

    /**
     * 查询订单物流信息
     * 
     * @param id 订单物流信息主键
     * @return 订单物流信息
     */
    @Override
    public VisaOrderLogistics selectVisaOrderLogisticsById(Long id)
    {
        return visaOrderLogisticsMapper.selectVisaOrderLogisticsById(id);
    }

    /**
     * 查询订单物流信息列表
     * 
     * @param visaOrderLogistics 订单物流信息
     * @return 订单物流信息
     */
    @Override
    public List<VisaOrderLogistics> selectVisaOrderLogisticsList(VisaOrderLogistics visaOrderLogistics)
    {
        return visaOrderLogisticsMapper.selectVisaOrderLogisticsList(visaOrderLogistics);
    }

    /**
     * 新增订单物流信息
     * 
     * @param visaOrderLogistics 订单物流信息
     * @return 结果
     */
    @Override
    public int insertVisaOrderLogistics(VisaOrderLogistics visaOrderLogistics)
    {
        visaOrderLogistics.setCreateTime(DateUtils.getNowDate());
        return visaOrderLogisticsMapper.insertVisaOrderLogistics(visaOrderLogistics);
    }

    /**
     * 修改订单物流信息
     * 
     * @param visaOrderLogistics 订单物流信息
     * @return 结果
     */
    @Override
    public int updateVisaOrderLogistics(VisaOrderLogistics visaOrderLogistics)
    {
        return visaOrderLogisticsMapper.updateVisaOrderLogistics(visaOrderLogistics);
    }

    /**
     * 批量删除订单物流信息
     * 
     * @param ids 需要删除的订单物流信息主键
     * @return 结果
     */
    @Override
    public int deleteVisaOrderLogisticsByIds(Long[] ids)
    {
        return visaOrderLogisticsMapper.deleteVisaOrderLogisticsByIds(ids);
    }

    /**
     * 删除订单物流信息信息
     * 
     * @param id 订单物流信息主键
     * @return 结果
     */
    @Override
    public int deleteVisaOrderLogisticsById(Long id)
    {
        return visaOrderLogisticsMapper.deleteVisaOrderLogisticsById(id);
    }
}
