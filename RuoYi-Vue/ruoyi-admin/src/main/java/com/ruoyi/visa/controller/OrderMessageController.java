package com.ruoyi.visa.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.visa.domain.OrderMessage;
import com.ruoyi.visa.service.IOrderMessageService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 订单留言记录Controller
 * 
 * @author bella
 * @date 2026-03-09
 */
@RestController
@RequestMapping("/visa/message")
public class OrderMessageController extends BaseController
{
    @Autowired
    private IOrderMessageService orderMessageService;

    /**
     * 查询订单留言记录列表
     */
    @PreAuthorize("@ss.hasPermi('visa:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(OrderMessage orderMessage)
    {
        startPage();
        List<OrderMessage> list = orderMessageService.selectOrderMessageList(orderMessage);
        return getDataTable(list);
    }

    /**
     * 导出订单留言记录列表
     */
    @PreAuthorize("@ss.hasPermi('visa:message:export')")
    @Log(title = "订单留言记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OrderMessage orderMessage)
    {
        List<OrderMessage> list = orderMessageService.selectOrderMessageList(orderMessage);
        ExcelUtil<OrderMessage> util = new ExcelUtil<OrderMessage>(OrderMessage.class);
        util.exportExcel(response, list, "订单留言记录数据");
    }

    /**
     * 获取订单留言记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:message:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(orderMessageService.selectOrderMessageById(id));
    }

    /**
     * 新增订单留言记录
     */
    @PreAuthorize("@ss.hasPermi('visa:message:add')")
    @Log(title = "订单留言记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OrderMessage orderMessage)
    {
        return toAjax(orderMessageService.insertOrderMessage(orderMessage));
    }

    /**
     * 修改订单留言记录
     */
    @PreAuthorize("@ss.hasPermi('visa:message:edit')")
    @Log(title = "订单留言记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OrderMessage orderMessage)
    {
        return toAjax(orderMessageService.updateOrderMessage(orderMessage));
    }

    /**
     * 删除订单留言记录
     */
    @PreAuthorize("@ss.hasPermi('visa:message:remove')")
    @Log(title = "订单留言记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(orderMessageService.deleteOrderMessageByIds(ids));
    }
}
