package com.ruoyi.visa.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaOrderMapper;
import com.ruoyi.visa.domain.VisaOrder;
import com.ruoyi.visa.service.IVisaOrderService;

/**
 * 签证订单Service业务层处理
 * 
 * @author bella
 * @date 2026-01-30
 */
@Service
public class VisaOrderServiceImpl implements IVisaOrderService 
{
    @Autowired
    private VisaOrderMapper visaOrderMapper;

    /**
     * 查询签证订单
     * 
     * @param id 签证订单主键
     * @return 签证订单
     */
    @Override
    public VisaOrder selectVisaOrderById(Long id)
    {
        return visaOrderMapper.selectVisaOrderById(id);
    }

    /**
     * 查询签证订单列表
     * 
     * @param visaOrder 签证订单
     * @return 签证订单
     */
    @Override
    public List<VisaOrder> selectVisaOrderList(VisaOrder visaOrder)
    {
        return visaOrderMapper.selectVisaOrderList(visaOrder);
    }

    /**
     * 新增签证订单
     * 
     * @param visaOrder 签证订单
     * @return 结果
     */
    @Override
    public int insertVisaOrder(VisaOrder visaOrder)
    {
        visaOrder.setCreateTime(DateUtils.getNowDate());
        return visaOrderMapper.insertVisaOrder(visaOrder);
    }

    /**
     * 修改签证订单
     * 
     * @param visaOrder 签证订单
     * @return 结果
     */
    @Override
    public int updateVisaOrder(VisaOrder visaOrder)
    {
        visaOrder.setUpdateTime(DateUtils.getNowDate());
        return visaOrderMapper.updateVisaOrder(visaOrder);
    }

    /**
     * 批量删除签证订单
     * 
     * @param ids 需要删除的签证订单主键
     * @return 结果
     */
    @Override
    public int deleteVisaOrderByIds(Long[] ids)
    {
        return visaOrderMapper.deleteVisaOrderByIds(ids);
    }

    /**
     * 删除签证订单信息
     * 
     * @param id 签证订单主键
     * @return 结果
     */
    @Override
    public int deleteVisaOrderById(Long id)
    {
        return visaOrderMapper.deleteVisaOrderById(id);
    }

    @Override
    public VisaOrder selectVisaOrderByOrderNo(String orderNo) {
        return visaOrderMapper.selectVisaOrderByOrderNo(orderNo);
    }
}
