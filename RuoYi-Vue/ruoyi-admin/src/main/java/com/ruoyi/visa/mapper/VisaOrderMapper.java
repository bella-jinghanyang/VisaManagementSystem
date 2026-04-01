package com.ruoyi.visa.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.visa.domain.VisaOrder;
import org.apache.ibatis.annotations.Param;

/**
 * 签证订单Mapper接口
 * 
 * @author bella
 * @date 2026-01-30
 */
public interface VisaOrderMapper 
{
    /**
     * 查询签证订单
     * 
     * @param id 签证订单主键
     * @return 签证订单
     */
    public VisaOrder selectVisaOrderById(Long id);

    /**
     * 查询签证订单列表
     * 
     * @param visaOrder 签证订单
     * @return 签证订单集合
     */
    public List<VisaOrder> selectVisaOrderList(VisaOrder visaOrder);

    /**
     * 新增签证订单
     * 
     * @param visaOrder 签证订单
     * @return 结果
     */
    public int insertVisaOrder(VisaOrder visaOrder);

    /**
     * 修改签证订单
     * 
     * @param visaOrder 签证订单
     * @return 结果
     */
    public int updateVisaOrder(VisaOrder visaOrder);

    /**
     * 删除签证订单
     * 
     * @param id 签证订单主键
     * @return 结果
     */
    public int deleteVisaOrderById(Long id);

    /**
     * 批量删除签证订单
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteVisaOrderByIds(Long[] ids);

    /**
     * 根据订单号查询签证订单
     * @param orderNo 订单号
     * @return 签证订单
     */
    VisaOrder selectVisaOrderByOrderNo(String orderNo);

    /**
     * 查询有过沟通记录的订单列表
     */
    public List<VisaOrder> selectChatUserList();

    // 查询首页各项统计数值
    Map<String, Object> selectIndexStats();

    // 查询热门国家分布
    List<Map<String, Object>> selectTopDestinations();

    // 查询滞留预警订单
    List<Map<String, Object>> selectWarningOrders();


}

