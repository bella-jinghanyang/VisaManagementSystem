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
import com.ruoyi.visa.domain.VisaDistrict;
import com.ruoyi.visa.service.IVisaDistrictService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 领区配置Controller
 * 
 * @author bella
 * @date 2026-01-23
 */
@RestController
@RequestMapping("/visa/district")
public class VisaDistrictController extends BaseController
{
    @Autowired
    private IVisaDistrictService visaDistrictService;

    /**
     * 查询领区配置列表
     */
    @PreAuthorize("@ss.hasPermi('visa:district:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaDistrict visaDistrict)
    {
        startPage();
        List<VisaDistrict> list = visaDistrictService.selectVisaDistrictList(visaDistrict);
        return getDataTable(list);
    }

    /**
     * 导出领区配置列表
     */
    @PreAuthorize("@ss.hasPermi('visa:district:export')")
    @Log(title = "领区配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaDistrict visaDistrict)
    {
        List<VisaDistrict> list = visaDistrictService.selectVisaDistrictList(visaDistrict);
        ExcelUtil<VisaDistrict> util = new ExcelUtil<VisaDistrict>(VisaDistrict.class);
        util.exportExcel(response, list, "领区配置数据");
    }

    /**
     * 获取领区配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:district:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaDistrictService.selectVisaDistrictById(id));
    }

    /**
     * 新增领区配置
     */
    @PreAuthorize("@ss.hasPermi('visa:district:add')")
    @Log(title = "领区配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaDistrict visaDistrict)
    {
        return toAjax(visaDistrictService.insertVisaDistrict(visaDistrict));
    }

    /**
     * 修改领区配置
     */
    @PreAuthorize("@ss.hasPermi('visa:district:edit')")
    @Log(title = "领区配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaDistrict visaDistrict)
    {
        return toAjax(visaDistrictService.updateVisaDistrict(visaDistrict));
    }

    /**
     * 删除领区配置
     */
    @PreAuthorize("@ss.hasPermi('visa:district:remove')")
    @Log(title = "领区配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaDistrictService.deleteVisaDistrictByIds(ids));
    }

    /**
     * 修改领区状态
     */
    @PreAuthorize("@ss.hasPermi('visa:district:edit')")
    @Log(title = "领区状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{id}/{status}")
    public AjaxResult changeStatus(@PathVariable Long id, @PathVariable String status)
    {
        return toAjax(visaDistrictService.updateDistrictStatus(id, status));
    }
}
