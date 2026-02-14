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
}
