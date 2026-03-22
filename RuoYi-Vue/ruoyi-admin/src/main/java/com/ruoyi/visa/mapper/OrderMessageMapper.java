package com.ruoyi.visa.mapper;

import java.util.List;
import com.ruoyi.visa.domain.OrderMessage;

/**
 * 订单留言记录Mapper接口
 * 
 * @author bella
 * @date 2026-03-09
 */
public interface OrderMessageMapper 
{
    /**
     * 查询订单留言记录
     * 
     * @param id 订单留言记录主键
     * @return 订单留言记录
     */
    public OrderMessage selectOrderMessageById(Long id);

    /**
     * 查询订单留言记录列表
     * 
     * @param orderMessage 订单留言记录
     * @return 订单留言记录集合
     */
    public List<OrderMessage> selectOrderMessageList(OrderMessage orderMessage);

    /**
     * 新增订单留言记录
     * 
     * @param orderMessage 订单留言记录
     * @return 结果
     */
    public int insertOrderMessage(OrderMessage orderMessage);

    /**
     * 修改订单留言记录
     * 
     * @param orderMessage 订单留言记录
     * @return 结果
     */
    public int updateOrderMessage(OrderMessage orderMessage);

    /**
     * 删除订单留言记录
     * 
     * @param id 订单留言记录主键
     * @return 结果
     */
    public int deleteOrderMessageById(Long id);

    /**
     * 批量删除订单留言记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrderMessageByIds(Long[] ids);
}
