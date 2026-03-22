package com.ruoyi.visa.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.OrderMessageMapper;
import com.ruoyi.visa.domain.OrderMessage;
import com.ruoyi.visa.service.IOrderMessageService;

/**
 * 订单留言记录Service业务层处理
 * 
 * @author bella
 * @date 2026-03-09
 */
@Service
public class OrderMessageServiceImpl implements IOrderMessageService 
{
    @Autowired
    private OrderMessageMapper orderMessageMapper;

    /**
     * 查询订单留言记录
     * 
     * @param id 订单留言记录主键
     * @return 订单留言记录
     */
    @Override
    public OrderMessage selectOrderMessageById(Long id)
    {
        return orderMessageMapper.selectOrderMessageById(id);
    }

    /**
     * 查询订单留言记录列表
     * 
     * @param orderMessage 订单留言记录
     * @return 订单留言记录
     */
    @Override
    public List<OrderMessage> selectOrderMessageList(OrderMessage orderMessage)
    {
        return orderMessageMapper.selectOrderMessageList(orderMessage);
    }

    /**
     * 新增订单留言记录
     * 
     * @param orderMessage 订单留言记录
     * @return 结果
     */
    @Override
    public int insertOrderMessage(OrderMessage orderMessage)
    {
        orderMessage.setCreateTime(DateUtils.getNowDate());
        return orderMessageMapper.insertOrderMessage(orderMessage);
    }

    /**
     * 修改订单留言记录
     * 
     * @param orderMessage 订单留言记录
     * @return 结果
     */
    @Override
    public int updateOrderMessage(OrderMessage orderMessage)
    {
        return orderMessageMapper.updateOrderMessage(orderMessage);
    }

    /**
     * 批量删除订单留言记录
     * 
     * @param ids 需要删除的订单留言记录主键
     * @return 结果
     */
    @Override
    public int deleteOrderMessageByIds(Long[] ids)
    {
        return orderMessageMapper.deleteOrderMessageByIds(ids);
    }

    /**
     * 删除订单留言记录信息
     * 
     * @param id 订单留言记录主键
     * @return 结果
     */
    @Override
    public int deleteOrderMessageById(Long id)
    {
        return orderMessageMapper.deleteOrderMessageById(id);
    }
}
