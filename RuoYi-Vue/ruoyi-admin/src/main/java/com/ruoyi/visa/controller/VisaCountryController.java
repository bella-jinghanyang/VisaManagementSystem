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
import com.ruoyi.visa.domain.VisaCountry;
import com.ruoyi.visa.service.IVisaCountryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 国家配置Controller
 * 
 * @author ruoyi
 * @date 2026-01-23
 */
@RestController
@RequestMapping("/visa/country")
public class VisaCountryController extends BaseController
{
    @Autowired
    private IVisaCountryService visaCountryService;

    /**
     * 查询国家配置列表
     */
    @PreAuthorize("@ss.hasPermi('visa:country:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaCountry visaCountry)
    {
        startPage();
        List<VisaCountry> list = visaCountryService.selectVisaCountryList(visaCountry);
        return getDataTable(list);
    }

    /**
     * 导出国家配置列表
     */
    @PreAuthorize("@ss.hasPermi('visa:country:export')")
    @Log(title = "国家配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaCountry visaCountry)
    {
        List<VisaCountry> list = visaCountryService.selectVisaCountryList(visaCountry);
        ExcelUtil<VisaCountry> util = new ExcelUtil<VisaCountry>(VisaCountry.class);
        util.exportExcel(response, list, "国家配置数据");
    }

    /**
     * 获取国家配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:country:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaCountryService.selectVisaCountryById(id));
    }

    /**
     * 新增国家配置
     */
    @PreAuthorize("@ss.hasPermi('visa:country:add')")
    @Log(title = "国家配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaCountry visaCountry)
    {
        return toAjax(visaCountryService.insertVisaCountry(visaCountry));
    }

    /**
     * 修改国家配置
     */
    @PreAuthorize("@ss.hasPermi('visa:country:edit')")
    @Log(title = "国家配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaCountry visaCountry)
    {
        return toAjax(visaCountryService.updateVisaCountry(visaCountry));
    }

    /**
     * 删除国家配置
     */
    @PreAuthorize("@ss.hasPermi('visa:country:remove')")
    @Log(title = "国家配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaCountryService.deleteVisaCountryByIds(ids));
    }

    /**
     * 修改国家状态
     */
    @PreAuthorize("@ss.hasPermi('visa:country:edit')")
    @Log(title = "国家状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{id}/{status}")
    public AjaxResult changeStatus(@PathVariable Long id, @PathVariable String status)
    {
        return toAjax(visaCountryService.updateCountryStatus(id, status));
    }
}
