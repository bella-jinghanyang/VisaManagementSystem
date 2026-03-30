package com.ruoyi.visa.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.visa.domain.VisaOrderApplicant;
import com.ruoyi.visa.domain.VisaOrderLogistics;
import com.ruoyi.visa.domain.VisaProduct;
import com.ruoyi.visa.mapper.VisaOrderApplicantMapper;
import com.ruoyi.visa.mapper.VisaOrderLogisticsMapper;
import com.ruoyi.visa.service.IVisaProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaOrderMapper;
import com.ruoyi.visa.domain.VisaOrder;
import com.ruoyi.visa.service.IVisaOrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


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

    @Autowired
    private IVisaProductService productService;

    @Autowired
    private VisaOrderLogisticsMapper logisticsMapper; // 注入物流Mapper


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
    @Transactional
    public int updateVisaOrder(VisaOrder visaOrder)
    {
        visaOrder.setUpdateTime(DateUtils.getNowDate());

        // ★★★ PM 逻辑：当状态变为 5 (待收货) 时，生成“寄回给客户”的物流记录 ★★★
        if (visaOrder.getStatus() != null && visaOrder.getStatus() == 5L) {
            // 1. 获取数据库中完整的原始订单信息（为了拿到下单时的 mailing_address JSON）
            VisaOrder dbOrder = visaOrderMapper.selectVisaOrderById(visaOrder.getId());

            VisaOrderLogistics logistics = new VisaOrderLogistics();
            logistics.setOrderId(visaOrder.getId());
            logistics.setOrderNo(dbOrder.getOrderNo());
            logistics.setDirection(2); // 2: 中介 -> 客户

            // 2. 填充单号和快递公司（从前端传来的临时属性中取）
            logistics.setTrackingNo(visaOrder.getTrackingNumber());
            String company = (String) visaOrder.getParams().get("courierCompany");
            logistics.setCourierCompany(StringUtils.isNotEmpty(company) ? company : "顺丰速运");

            // 3. 设置【寄件人】（中介固定信息）
            logistics.setSenderName("全球通签证中心");
            logistics.setSenderPhone("010-88888888");

            // 4. ★ 核心逻辑：解析地址 JSON 并组装成你指定的格式 ★
            String addressJson = dbOrder.getMailingAddress();
            if (StringUtils.isNotEmpty(addressJson)) {
                // 解析下单时存的 JSON: {"contactName":"张三","contactPhone":"138...","address":"北京..."}
                Map<String, String> addrMap = JSON.parseObject(addressJson, Map.class);

                String formattedAddress = String.format("收件人：%s, 电话：%s, 地址：%s",
                        addrMap.get("contactName"),
                        addrMap.get("contactPhone"),
                        addrMap.get("address"));

                // 存入物流表的 mail_address 字段
                logistics.setMailAddress(formattedAddress);
            }

            logistics.setStatus(1); // 1: 已寄出/运输中
            logistics.setCreateTime(DateUtils.getNowDate());

            // 5. 插入物流表记录
            logisticsMapper.insertVisaOrderLogistics(logistics);
        }

        // 6. 执行主表更新（只更新状态、备注、结果等，此时XML里已删除了不存在的列）
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


    /**
     * 场景 A: 管理员在 Tab 1 点击“初步合格”
     * 实现自动化分流：根据产品属性决定去 9(需原件) 还是 4/7(电子签)
     */


    @Override
    @Transactional
    public int approveElectronicMaterials(Long orderId, String auditRemark) {
        // 1. 获取最新订单和产品配置
        VisaOrder order = visaOrderMapper.selectVisaOrderById(orderId);
        VisaProduct product = productService.selectVisaProductById(order.getProductId());

        // 2. 自动化分流判断
        if (product.getIsPhysicalRequired() != null && product.getIsPhysicalRequired() == 1) {
            // ★ 核心修改：直接修改 order 对象的属性，不要分两次更新数据库
            order.setStatus(9L);
        } else {
            order.setStatus(product.getIsInterviewRequired() == 1 ? 7L : 4L);
        }

        // 3. 设置备注和时间
        order.setAuditRemark(auditRemark);
        order.setUpdateTime(DateUtils.getNowDate());

        // 🕵️ 打印最终确认
        System.out.println(">>> [审批最终执行] 单号: " + order.getOrderNo() + " 最终状态将存入: " + order.getStatus());

        // 4. 只调用这一次更新！
        // 由于你已经清理了 XML 里的无效字段，这次更新会包含 status=9 和 audit_remark
        return visaOrderMapper.updateVisaOrder(order);
    }

    /**
     * 场景 B: 管理员在 Tab 2 点击“确认收货入库”
     * 实现从状态 10 到后续办理的流转
     */
    @Transactional
    public int handleReceivePhysical(Long orderId) {
        VisaOrder order = visaOrderMapper.selectVisaOrderById(orderId);

        // 1. 更新订单状态
        // 既然原件已收悉，直接进入 7(需面试) 或 4(直接办理)
        order.setStatus(order.getIsInterviewRequired() == 1 ? 7L : 4L);
        order.setUpdateTime(DateUtils.getNowDate());
        visaOrderMapper.updateVisaOrder(order);

        // 2. 更新物流表状态 (可选逻辑)
        // 根据 orderId 查找 direction=1 的最新一条物流，将其 receive_time 更新
        // logisticsMapper.updateReceiveTimeByOrderId(orderId, new Date());

        return 1;
    }





}
