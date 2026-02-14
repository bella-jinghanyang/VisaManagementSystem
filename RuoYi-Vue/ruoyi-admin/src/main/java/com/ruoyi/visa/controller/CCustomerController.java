package com.ruoyi.visa.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.utils.SecurityUtils;
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
import com.ruoyi.visa.domain.CCustomer;
import com.ruoyi.visa.service.ICCustomerService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * C端客户Controller
 * 
 * @author bella
 * @date 2026-01-26
 */
@RestController
@RequestMapping("/visa/customer")
public class CCustomerController extends BaseController
{
    @Autowired
    private ICCustomerService cCustomerService;

    /**
     * 查询C端客户列表
     */
    @PreAuthorize("@ss.hasPermi('visa:customer:list')")
    @GetMapping("/list")
    public TableDataInfo list(CCustomer cCustomer)
    {
        startPage();
        List<CCustomer> list = cCustomerService.selectCCustomerList(cCustomer);
        return getDataTable(list);
    }

    /**
     * 导出C端客户列表
     */
    @PreAuthorize("@ss.hasPermi('visa:customer:export')")
    @Log(title = "C端客户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CCustomer cCustomer)
    {
        List<CCustomer> list = cCustomerService.selectCCustomerList(cCustomer);
        ExcelUtil<CCustomer> util = new ExcelUtil<CCustomer>(CCustomer.class);
        util.exportExcel(response, list, "C端客户数据");
    }

    /**
     * 获取C端客户详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:customer:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(cCustomerService.selectCCustomerById(id));
    }

    /**
     * 新增C端客户
     */
    @PreAuthorize("@ss.hasPermi('visa:customer:add')")
    @Log(title = "C端客户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CCustomer cCustomer)
    {
        return toAjax(cCustomerService.insertCCustomer(cCustomer));
    }

    /**
     * 修改C端客户
     */
    @PreAuthorize("@ss.hasPermi('visa:customer:edit')")
    @Log(title = "C端客户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CCustomer cCustomer)
    {
        return toAjax(cCustomerService.updateCCustomer(cCustomer));
    }

    /**
     * 删除C端客户
     */
    @PreAuthorize("@ss.hasPermi('visa:customer:remove')")
    @Log(title = "C端客户", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(cCustomerService.deleteCCustomerByIds(ids));
    }

    /**
     * 修改用户状态
     */
    @PreAuthorize("@ss.hasPermi('visa:customer:edit')")
    @Log(title = "客户状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{id}/{status}")
    public AjaxResult changeStatus(@PathVariable Long id, @PathVariable String status)
    {
        return toAjax(cCustomerService.updateCustomerStatus(id, status));
    }
}
