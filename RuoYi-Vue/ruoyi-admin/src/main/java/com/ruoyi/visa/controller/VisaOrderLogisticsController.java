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
import com.ruoyi.visa.domain.VisaOrderLogistics;
import com.ruoyi.visa.service.IVisaOrderLogisticsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 订单物流信息Controller
 * 
 * @author bella
 * @date 2026-03-29
 */
@RestController
@RequestMapping("/visa/logistics")
public class VisaOrderLogisticsController extends BaseController
{
    @Autowired
    private IVisaOrderLogisticsService visaOrderLogisticsService;

    /**
     * 查询订单物流信息列表
     */
    @PreAuthorize("@ss.hasPermi('visa:logistics:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaOrderLogistics visaOrderLogistics)
    {
        startPage();
        List<VisaOrderLogistics> list = visaOrderLogisticsService.selectVisaOrderLogisticsList(visaOrderLogistics);
        return getDataTable(list);
    }

    /**
     * 导出订单物流信息列表
     */
    @PreAuthorize("@ss.hasPermi('visa:logistics:export')")
    @Log(title = "订单物流信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaOrderLogistics visaOrderLogistics)
    {
        List<VisaOrderLogistics> list = visaOrderLogisticsService.selectVisaOrderLogisticsList(visaOrderLogistics);
        ExcelUtil<VisaOrderLogistics> util = new ExcelUtil<VisaOrderLogistics>(VisaOrderLogistics.class);
        util.exportExcel(response, list, "订单物流信息数据");
    }

    /**
     * 获取订单物流信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:logistics:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaOrderLogisticsService.selectVisaOrderLogisticsById(id));
    }

    /**
     * 新增订单物流信息
     */
    @PreAuthorize("@ss.hasPermi('visa:logistics:add')")
    @Log(title = "订单物流信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaOrderLogistics visaOrderLogistics)
    {
        return toAjax(visaOrderLogisticsService.insertVisaOrderLogistics(visaOrderLogistics));
    }

    /**
     * 修改订单物流信息
     */
    @PreAuthorize("@ss.hasPermi('visa:logistics:edit')")
    @Log(title = "订单物流信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaOrderLogistics visaOrderLogistics)
    {
        return toAjax(visaOrderLogisticsService.updateVisaOrderLogistics(visaOrderLogistics));
    }

    /**
     * 删除订单物流信息
     */
    @PreAuthorize("@ss.hasPermi('visa:logistics:remove')")
    @Log(title = "订单物流信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaOrderLogisticsService.deleteVisaOrderLogisticsByIds(ids));
    }
}
