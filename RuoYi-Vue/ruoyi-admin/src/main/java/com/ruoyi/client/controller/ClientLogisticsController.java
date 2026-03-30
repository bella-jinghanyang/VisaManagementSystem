
package com.ruoyi.client.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.visa.domain.VisaOrder;
import com.ruoyi.visa.domain.VisaOrderLogistics;
import com.ruoyi.visa.service.IVisaOrderLogisticsService;
import com.ruoyi.visa.service.IVisaOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/logistics")
public class ClientLogisticsController extends BaseController {

    @Autowired
    private IVisaOrderLogisticsService logisticsService;
    @Autowired
    private IVisaOrderService orderService;

    /**
     * 用户提交寄送单号
     */
    @Anonymous
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody VisaOrderLogistics logistics) {
        // 1. 设置基础信息
        logistics.setDirection(1); // 1-用户寄给中介
        logistics.setStatus(1); // 1-已寄出/运输中
        logistics.setCreateTime(DateUtils.getNowDate());

        // 2. 保存物流记录
        int rows = logisticsService.insertVisaOrderLogistics(logistics);

        if (rows > 0) {
            // 3. 同步修改主订单状态为 2 (审核中)，代表资料已收齐，中介准备二次核对原件
            VisaOrder order = new VisaOrder();
            order.setId(logistics.getOrderId());
            order.setStatus(2L);
            orderService.updateVisaOrder(order);
            return success("单号录入成功，请保留快递底单");
        }
        return error("提交失败");
    }
}