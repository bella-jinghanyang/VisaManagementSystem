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
import com.ruoyi.visa.domain.VisaOrderApplicant;
import com.ruoyi.visa.service.IVisaOrderApplicantService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 订单申请人基本信息Controller
 * 
 * @author bella
 * @date 2026-03-28
 */
@RestController
@RequestMapping("/visa/applicant")
public class VisaOrderApplicantController extends BaseController
{
    @Autowired
    private IVisaOrderApplicantService visaOrderApplicantService;

    /**
     * 查询订单申请人基本信息列表
     */
    @PreAuthorize("@ss.hasPermi('visa:applicant:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaOrderApplicant visaOrderApplicant)
    {
        startPage();
        List<VisaOrderApplicant> list = visaOrderApplicantService.selectVisaOrderApplicantList(visaOrderApplicant);
        return getDataTable(list);
    }

    /**
     * 导出订单申请人基本信息列表
     */
    @PreAuthorize("@ss.hasPermi('visa:applicant:export')")
    @Log(title = "订单申请人基本信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaOrderApplicant visaOrderApplicant)
    {
        List<VisaOrderApplicant> list = visaOrderApplicantService.selectVisaOrderApplicantList(visaOrderApplicant);
        ExcelUtil<VisaOrderApplicant> util = new ExcelUtil<VisaOrderApplicant>(VisaOrderApplicant.class);
        util.exportExcel(response, list, "订单申请人基本信息数据");
    }

    /**
     * 获取订单申请人基本信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:applicant:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaOrderApplicantService.selectVisaOrderApplicantById(id));
    }

    /**
     * 新增订单申请人基本信息
     */
    @PreAuthorize("@ss.hasPermi('visa:applicant:add')")
    @Log(title = "订单申请人基本信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaOrderApplicant visaOrderApplicant)
    {
        return toAjax(visaOrderApplicantService.insertVisaOrderApplicant(visaOrderApplicant));
    }

    /**
     * 修改订单申请人基本信息
     */
    @PreAuthorize("@ss.hasPermi('visa:applicant:edit')")
    @Log(title = "订单申请人基本信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaOrderApplicant visaOrderApplicant)
    {
        return toAjax(visaOrderApplicantService.updateVisaOrderApplicant(visaOrderApplicant));
    }

    /**
     * 删除订单申请人基本信息
     */
    @PreAuthorize("@ss.hasPermi('visa:applicant:remove')")
    @Log(title = "订单申请人基本信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaOrderApplicantService.deleteVisaOrderApplicantByIds(ids));
    }
}
