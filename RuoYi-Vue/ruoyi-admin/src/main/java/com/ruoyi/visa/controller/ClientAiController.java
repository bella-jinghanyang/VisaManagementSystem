package com.ruoyi.visa.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.system.service.VisaAiService;
import com.ruoyi.visa.domain.OrderMessage;
import com.ruoyi.visa.domain.VisaKnowledge;
import com.ruoyi.visa.service.IOrderMessageService;
import com.ruoyi.visa.service.IVisaKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/client/ai")
public class ClientAiController extends BaseController {

    @Autowired
    private VisaAiService aiService;

    @Autowired
    private IVisaKnowledgeService knowledgeService;

    @Autowired
    private IOrderMessageService messageService;

    /**
     * AI 咨询接口（合并版）
     * 这里的 orderId 设置为 required = false，表示如果不传也可以访问
     */
    @Anonymous
    @GetMapping("/chat")
    public AjaxResult chat(@RequestParam("q") String q,
                           @RequestParam(value = "orderId", required = false) Long orderId,
                           @RequestParam(value = "customerId", required = false) Long customerId) {
        // 1. 保存用户的问题
        OrderMessage userMsg = new OrderMessage();
        userMsg.setOrderId(orderId != null ? orderId : 0L);
        userMsg.setCustomerId(customerId != null ? customerId : 0L); // ★ 修复 ID 丢失
        userMsg.setSenderType(1);
        userMsg.setContent(q);
        userMsg.setIsAi("1"); // ★ 标记为 AI 消息
        messageService.insertOrderMessage(userMsg);

        // 2. 获取 AI 回复
        String aiAnswer = aiService.getAiResponse(q, "参考资料...");

        // 3. 保存 AI 的回答
        OrderMessage aiMsg = new OrderMessage();
        aiMsg.setOrderId(orderId != null ? orderId : 0L);
        aiMsg.setCustomerId(customerId != null ? customerId : 0L); // ★ 修复 ID 丢失
        aiMsg.setSenderType(2);
        aiMsg.setContent(aiAnswer);
        aiMsg.setIsAi("1"); // ★ 标记为 AI 消息
        messageService.insertOrderMessage(aiMsg);

        return AjaxResult.success("查询成功", aiAnswer);
    }
    /**
     * 获取历史聊天记录（合并版）
     * 自动兼容：只传orderId、只传customerId 或 两个都传的情况
     */
    @Anonymous
    @GetMapping("/history")
    public AjaxResult history(
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "customerId", required = false) Long customerId
    ) {
        OrderMessage query = new OrderMessage();

        // 1. 设置订单ID过滤（如果没传则默认为0，即通用咨询）
        query.setOrderId(orderId != null ? orderId : 0L);

        // 2. 设置客户ID过滤（实现身份隔离的关键）
        // 如果customerId为null，说明是匿名或者之前的老逻辑，建议根据业务需求处理
        if (customerId != null) {
            query.setCustomerId(customerId);
        }

        // 3. 执行查询
        List<OrderMessage> list = messageService.selectOrderMessageList(query);

        // 4. 排序（确保对话顺序正确）
        list.sort(java.util.Comparator.comparing(OrderMessage::getCreateTime));

        return success(list);
    }
}