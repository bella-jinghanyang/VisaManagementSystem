package com.ruoyi.visa.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.visa.domain.VisaOrderApplicant;
import com.ruoyi.visa.mapper.VisaOrderApplicantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaOrderMapper;
import com.ruoyi.visa.domain.VisaOrder;
import com.ruoyi.visa.service.IVisaOrderService;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private VisaOrderApplicantMapper applicantMapper;


    /**
     * 查询签证订单
     * 
     * @param id 签证订单主键
     * @return 签证订单
     */
    @Override
    public VisaOrder selectVisaOrderById(Long id)
    {
        // A. 查出订单主表信息
        VisaOrder order = visaOrderMapper.selectVisaOrderById(id);

        if (order != null) {
            // B. 准备查询条件：根据 order_id 查
            VisaOrderApplicant searchVo = new VisaOrderApplicant();
            searchVo.setOrderId(id);

            // C. 调用申请人的 Mapper 查出列表 (这个方法是 RuoYi 自动生成的)
            List<VisaOrderApplicant> applicants = applicantMapper.selectVisaOrderApplicantList(searchVo);

            // D. 塞进 order 对象（确保 VisaOrder.java 里有 applicantList 属性和 getter/setter）
            order.setApplicantList(applicants);

        }
        return order;
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
        // 1. 先查出订单主表列表（PageHelper 会在这里正常处理分页）
        List<VisaOrder> orderList = visaOrderMapper.selectVisaOrderList(visaOrder);

        // 2. 如果列表为空，直接返回
        if (orderList == null || orderList.isEmpty()) {
            return orderList;
        }

        // 3. 循环订单列表，为每个订单抓取申请人信息
        for (VisaOrder order : orderList) {
            VisaOrderApplicant searchVo = new VisaOrderApplicant();
            searchVo.setOrderId(order.getId());
            // 只查这个订单下的申请人
            List<VisaOrderApplicant> applicants = applicantMapper.selectVisaOrderApplicantList(searchVo);
            order.setApplicantList(applicants);
        }

        return orderList;
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

    @Override
    public List<VisaOrder> selectChatUserList()
    {
        return visaOrderMapper.selectChatUserList();
    }

    /**
     * 新增签证订单（带申请人信息）
     */
    @Override
    @Transactional // 必须加事务，确保主子表同时成功或失败
    public int insertVisaOrder(VisaOrder visaOrder)
    {
        visaOrder.setCreateTime(DateUtils.getNowDate());
        // 1. 插入订单主表
        int rows = visaOrderMapper.insertVisaOrder(visaOrder);

        // 2. 批量插入申请人子表
        insertApplicants(visaOrder);

        return rows;
    }

    /**
     * 循环插入申请人子表的方法
     */
    public void insertApplicants(VisaOrder visaOrder) {
        List<VisaOrderApplicant> applicantList = visaOrder.getApplicantList();
        Long orderId = visaOrder.getId();
        String orderNo = visaOrder.getOrderNo();

        if (StringUtils.isNotNull(applicantList)) {
            for (VisaOrderApplicant applicant : applicantList) {
                applicant.setOrderId(orderId); // 建立主外键关联
                applicant.setOrderNo(orderNo);
                applicant.setCreateTime(DateUtils.getNowDate());
                applicantMapper.insertVisaOrderApplicant(applicant); // 调用生成的插入方法

            }
        }
    }


}
