package com.ruoyi.visa.service;

import java.util.List;
import com.ruoyi.visa.domain.VisaOrder;
import org.springframework.transaction.annotation.Transactional;

/**
 * 签证订单Service接口
 * 
 * @author bella
 * @date 2026-01-30
 */
public interface IVisaOrderService 
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
     * 批量删除签证订单
     * 
     * @param ids 需要删除的签证订单主键集合
     * @return 结果
     */
    public int deleteVisaOrderByIds(Long[] ids);

    /**
     * 删除签证订单信息
     * 
     * @param id 签证订单主键
     * @return 结果
     */
    public int deleteVisaOrderById(Long id);

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

    /**
     * 循环插入申请人子表的方法
     */
    public void insertApplicants(VisaOrder visaOrder);

    /**
     * 管理员审核电子材料通过后的逻辑处理
     */
    public int approveElectronicMaterials(Long orderId, String auditRemark);

}
