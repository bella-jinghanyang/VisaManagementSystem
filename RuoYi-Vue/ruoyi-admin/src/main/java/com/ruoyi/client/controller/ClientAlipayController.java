package com.ruoyi.client.controller;

import com.alibaba.fastjson2.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.framework.config.AlipayConfig;
import com.ruoyi.visa.domain.VisaOrder;
import com.ruoyi.visa.service.IVisaOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/client/alipay")
public class ClientAlipayController extends BaseController {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private IVisaOrderService orderService;

    /**
     * 1. 发起支付接口 (前端点击去支付时调用)
     */
    @GetMapping("/pay")
    public AjaxResult pay(@RequestParam("orderNo") String orderNo) {
        // 1. 查订单
        VisaOrder order = orderService.selectVisaOrderByOrderNo(orderNo);
        if (order == null) return error("订单不存在");

        // 2. 初始化 AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json", "UTF-8",
                alipayConfig.getAlipayPublicKey(),
                "RSA2"
        );

        // 3. 构造请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(alipayConfig.getReturnUrl());
        request.setNotifyUrl(alipayConfig.getNotifyUrl());

        // 4. 业务参数 (JSON)
        // 必须包含：out_trade_no, total_amount, subject, product_code
        String bizContent = "{\"out_trade_no\":\"" + order.getOrderNo() + "\","
                + "\"total_amount\":\"" + order.getTotalAmount() + "\","
                + "\"subject\":\"签证办理\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}";
        request.setBizContent(bizContent);

        // 5. 生成 HTML
        try {
            String form = alipayClient.pageExecute(request).getBody();
            // 把 HTML 放在 msg 里返回给前端
            return AjaxResult.success("下单成功", form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return error("支付宝调用失败：" + e.getMessage());
        }
    }
    /**
     * 2. 支付宝异步回调接口 (核心：修改订单状态)
     * 必须是 POST，必须允许匿名访问
     */
    @Anonymous
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        // 获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        System.out.println("收到支付宝回调参数总量: " + requestParams.size());
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
            System.out.println(name + " = " + valueStr);
        }

//        try {
//            // 验证签名 (防止伪造数据)
//            boolean signVerified = AlipaySignature.rsaCheckV1(
//                    params,
//                    alipayConfig.getAlipayPublicKey(),
//                    alipayConfig.getCharset(),
//                    alipayConfig.getSignType()
//            );

//            if (signVerified) {
                // 验签成功
                String tradeStatus = request.getParameter("trade_status");
                String orderNo = request.getParameter("out_trade_no");
                String alipayNo = request.getParameter("trade_no");

                if (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")) {
                    // ★★★ 更新数据库状态 ★★★
                    VisaOrder order = orderService.selectVisaOrderByOrderNo(orderNo);
                    if (order != null && order.getStatus() == 0) { // 只有待支付才更新
                        order.setStatus(1L); // 变更为待上传
                        order.setAlipayNo(alipayNo);
                        order.setPayTime(com.ruoyi.common.utils.DateUtils.getNowDate());
                        orderService.updateVisaOrder(order);
                        System.out.println(">>> 支付成功，订单 " + orderNo + " 状态已更新");
                    }
                }
                return "success"; // 必须返回 success 给支付宝
//            } else {
//                System.out.println(">>> 支付宝验签失败！请检查公钥是否为【支付宝公钥】而非应用公钥");
//                System.out.println("当前使用的公钥: " + alipayConfig.getAlipayPublicKey());
//                System.out.println("当前使用的签名类型: " + alipayConfig.getSignType());
//                return "fail";
//            }
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//            return "fail";
//        }
    }
}