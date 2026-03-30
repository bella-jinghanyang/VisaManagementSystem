package com.ruoyi.visa.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.visa.domain.OrderMessage;
import com.ruoyi.visa.service.IOrderMessageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.visa.domain.VisaOrder;
import com.ruoyi.visa.service.IVisaOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 签证订单Controller
 * 
 * @author bella
 * @date 2026-01-30
 */
@RestController
@RequestMapping("/visa/order")
public class VisaOrderController extends BaseController
{
    @Autowired
    private IVisaOrderService visaOrderService;

    @Autowired
    private IOrderMessageService orderMessageService;

    /**
     * 查询签证订单列表
     */
    @PreAuthorize("@ss.hasPermi('visa:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaOrder visaOrder)
    {
        startPage();
        List<VisaOrder> list = visaOrderService.selectVisaOrderList(visaOrder);
        return getDataTable(list);
    }

    /**
     * 导出签证订单列表
     */
    @PreAuthorize("@ss.hasPermi('visa:order:export')")
    @Log(title = "签证订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaOrder visaOrder)
    {
        List<VisaOrder> list = visaOrderService.selectVisaOrderList(visaOrder);
        ExcelUtil<VisaOrder> util = new ExcelUtil<VisaOrder>(VisaOrder.class);
        util.exportExcel(response, list, "签证订单数据");
    }

    /**
     * 获取签证订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:order:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaOrderService.selectVisaOrderById(id));
    }

    /**
     * 新增签证订单
     */
    @PreAuthorize("@ss.hasPermi('visa:order:add')")
    @Log(title = "签证订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaOrder visaOrder)
    {
        return toAjax(visaOrderService.insertVisaOrder(visaOrder));
    }

    /**
     * 修改签证订单
     */
    @PreAuthorize("@ss.hasPermi('visa:order:edit')")
    @Log(title = "签证订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaOrder visaOrder)
    {
        return toAjax(visaOrderService.updateVisaOrder(visaOrder));
    }

    /**
     * 删除签证订单
     */
    @PreAuthorize("@ss.hasPermi('visa:order:remove')")
    @Log(title = "签证订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaOrderService.deleteVisaOrderByIds(ids));
    }


    /**
     * 获取聊天历史 (管理员专用版)
     */
    @GetMapping("/chat/history")
    public AjaxResult getChatHistory(
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "customerId", required = false) Long customerId
    ) {
        OrderMessage query = new OrderMessage();
        query.setOrderId(orderId == null ? 0L : orderId);
        query.setCustomerId(customerId == null ? 0L : customerId);

        // ★★★ 核心修复：强制只查询非 AI 的消息 (人工对话) ★★★
        query.setIsAi("0");

        List<OrderMessage> list = orderMessageService.selectOrderMessageList(query);

        // 排序确保对话流畅
        list.sort(java.util.Comparator.comparing(OrderMessage::getCreateTime));

        return success(list);
    }

    /**
     * 获取在线客服的咨询用户列表 (管理员调用)
     */
    @PreAuthorize("@ss.hasPermi('visa:order:list')")
    @GetMapping("/chat/users")
    public TableDataInfo getChatUsers() {
        startPage();
        // 使用我们之前重构的那个 GROUP BY customer_id, order_id 的 SQL
        List<VisaOrder> list = visaOrderService.selectChatUserList();
        return getDataTable(list);
    }


    @PreAuthorize("@ss.hasPermi('visa:order:edit')")
    @Log(title = "签证订单", businessType = BusinessType.UPDATE)
    @PutMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable("id") Long id, @RequestBody VisaOrder order) {
        // 1. 调用你写在 Service 里那个带有“自动化分流”逻辑的方法
        // 同时也把管理员填写的备注（auditRemark）传进去保存
        int rows = visaOrderService.approveElectronicMaterials(id, order.getAuditRemark());
        return toAjax(rows);
    }
}
