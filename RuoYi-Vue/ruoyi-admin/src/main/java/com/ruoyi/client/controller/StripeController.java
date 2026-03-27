package com.ruoyi.client.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.visa.domain.VisaOrder;
import com.ruoyi.visa.service.IVisaOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/client/stripe")
public class StripeController {

    @Value("${stripe.secretKey}")
    private String secretKey;

    @Value("${stripe.frontendUrl}")
    private String frontendUrl;

    @Autowired
    private IVisaOrderService orderService;

    /**
     * 1. 创建支付会话
     */
    @Anonymous // 允许未登录时点击支付链接（或由前端带着Token调）
    @GetMapping("/pay")
    public AjaxResult createSession(@RequestParam("orderNo") String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return AjaxResult.error("订单号不能为空");
        }

        Stripe.apiKey = secretKey;

        // ★ 核心改动：使用最简单的 List 查询，避开复杂 JOIN 关联
        VisaOrder query = new VisaOrder();
        query.setOrderNo(orderNo);
        List<VisaOrder> list = orderService.selectVisaOrderList(query);
        VisaOrder order = (list != null && list.size() > 0) ? list.get(0) : null;

        if (order == null) {
            System.err.println(">>> [Stripe支付] 错误：数据库找不到该单号 -> " + orderNo);
            return AjaxResult.error("未找到订单信息，请稍后重试");
        }

        // 校验状态是否为 0 (待支付)
        if (order.getStatus() != 0L) {
            return AjaxResult.error("订单当前状态不可支付");
        }

        try {
            // 金额转换：元 -> 分
            long amount = order.getTotalAmount().multiply(new BigDecimal(100)).longValue();
            String successUrl = frontendUrl + "/user/orders?payResult=success&orderNo=" + orderNo;
            System.out.println(">>> 发送给 Stripe 的成功回跳地址: " + successUrl);

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    // 支付成功后的回跳地址
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(frontendUrl + "/#/user/orders?payResult=cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("cny")
                                                    .setUnitAmount(amount)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("签证申请服务：" + orderNo)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            System.out.println(">>> [Stripe支付] 会话创建成功: " + session.getUrl());
            return AjaxResult.success("下单成功", session.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("Stripe 网关异常: " + e.getMessage());
        }
    }

    /**
     * 2. 支付成功后的状态确认
     */
    @Anonymous // ★ 必须匿名，防止支付回跳时因 Token 失效导致无法更新状态
    @PostMapping("/confirm")
    public AjaxResult confirmPayment(@RequestBody VisaOrder param) {
        String orderNo = param.getOrderNo();
        System.out.println(">>> [Stripe确认] 开始同步订单状态，单号: " + orderNo);

        // 同样使用简单查询
        VisaOrder query = new VisaOrder();
        query.setOrderNo(orderNo);
        List<VisaOrder> list = orderService.selectVisaOrderList(query);
        VisaOrder order = (list != null && list.size() > 0) ? list.get(0) : null;

        if (order == null) {
            System.err.println(">>> [Stripe确认] 失败：找不到订单");
            return AjaxResult.error("同步失败：单号不存在");
        }

        // 仅在状态为 0 时更新为 1
        if (order.getStatus() != null && order.getStatus() == 0L) {
            order.setStatus(1L); // 0 -> 1 (待上传材料)
            order.setPayTime(new Date());
            order.setAlipayNo("STRIPE_" + System.currentTimeMillis());

            int rows = orderService.updateVisaOrder(order);
            System.out.println(">>> [Stripe确认] 数据库更新完成，影响行数: " + rows);
            return AjaxResult.success("支付同步成功");
        } else {
            System.out.println(">>> [Stripe确认] 订单状态已在之前变更过，无需处理。当前状态: " + order.getStatus());
            return AjaxResult.success("状态已更新");
        }
    }
}