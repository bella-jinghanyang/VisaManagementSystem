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
import com.ruoyi.visa.domain.VisaProduct;
import com.ruoyi.visa.service.IVisaProductService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 签证产品Controller
 * 
 * @author bella
 * @date 2026-01-24
 */
@RestController
@RequestMapping("/visa/product")
public class VisaProductController extends BaseController
{
    @Autowired
    private IVisaProductService visaProductService;

    /**
     * 查询签证产品列表
     */
    @PreAuthorize("@ss.hasPermi('visa:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaProduct visaProduct)
    {
        startPage();
        List<VisaProduct> list = visaProductService.selectVisaProductList(visaProduct);
        return getDataTable(list);
    }

    /**
     * 导出签证产品列表
     */
    @PreAuthorize("@ss.hasPermi('visa:product:export')")
    @Log(title = "签证产品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaProduct visaProduct)
    {
        List<VisaProduct> list = visaProductService.selectVisaProductList(visaProduct);
        ExcelUtil<VisaProduct> util = new ExcelUtil<VisaProduct>(VisaProduct.class);
        util.exportExcel(response, list, "签证产品数据");
    }

    /**
     * 获取签证产品详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:product:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaProductService.selectVisaProductById(id));
    }

    /**
     * 新增签证产品
     */
    @PreAuthorize("@ss.hasPermi('visa:product:add')")
    @Log(title = "签证产品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaProduct visaProduct)
    {
        return toAjax(visaProductService.insertVisaProduct(visaProduct));
    }

    /**
     * 修改签证产品
     */
    @PreAuthorize("@ss.hasPermi('visa:product:edit')")
    @Log(title = "签证产品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaProduct visaProduct)
    {
        return toAjax(visaProductService.updateVisaProduct(visaProduct));
    }

    /**
     * 删除签证产品
     */
    @PreAuthorize("@ss.hasPermi('visa:product:remove')")
    @Log(title = "签证产品", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaProductService.deleteVisaProductByIds(ids));
    }


    /**
     * 修改签证产品状态
     */
    @PreAuthorize("@ss.hasPermi('visa:product:edit')")
    @Log(title = "签证产品", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody VisaProduct visaProduct)
    {
        // 调用刚才写的新 Service 方法
        return toAjax(visaProductService.changeProductStatus(visaProduct));
    }



}
