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
import com.ruoyi.visa.domain.VisaType;
import com.ruoyi.visa.service.IVisaTypeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 签证类型Controller
 * 
 * @author bella
 * @date 2026-01-23
 */
@RestController
@RequestMapping("/visa/type")
public class VisaTypeController extends BaseController
{
    @Autowired
    private IVisaTypeService visaTypeService;

    /**
     * 查询签证类型列表
     */
    @PreAuthorize("@ss.hasPermi('visa:type:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaType visaType)
    {
        startPage();
        List<VisaType> list = visaTypeService.selectVisaTypeList(visaType);
        return getDataTable(list);
    }

    /**
     * 导出签证类型列表
     */
    @PreAuthorize("@ss.hasPermi('visa:type:export')")
    @Log(title = "签证类型", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaType visaType)
    {
        List<VisaType> list = visaTypeService.selectVisaTypeList(visaType);
        ExcelUtil<VisaType> util = new ExcelUtil<VisaType>(VisaType.class);
        util.exportExcel(response, list, "签证类型数据");
    }

    /**
     * 获取签证类型详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:type:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaTypeService.selectVisaTypeById(id));
    }

    /**
     * 新增签证类型
     */
    @PreAuthorize("@ss.hasPermi('visa:type:add')")
    @Log(title = "签证类型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaType visaType)
    {
        return toAjax(visaTypeService.insertVisaType(visaType));
    }

    /**
     * 修改签证类型
     */
    @PreAuthorize("@ss.hasPermi('visa:type:edit')")
    @Log(title = "签证类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaType visaType)
    {
        return toAjax(visaTypeService.updateVisaType(visaType));
    }

    /**
     * 删除签证类型
     */
    @PreAuthorize("@ss.hasPermi('visa:type:remove')")
    @Log(title = "签证类型", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaTypeService.deleteVisaTypeByIds(ids));
    }

    /**
     * 修改签证类型状态
     */
    @PreAuthorize("@ss.hasPermi('visa:type:edit')")
    @Log(title = "国家状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{id}/{status}")
    public AjaxResult changeStatus(@PathVariable Long id, @PathVariable String status)
    {
        return toAjax(visaTypeService.updateTypeStatus(id, status));
    }
}
