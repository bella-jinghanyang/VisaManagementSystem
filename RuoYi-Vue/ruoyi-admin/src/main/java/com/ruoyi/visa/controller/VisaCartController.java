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
import com.ruoyi.visa.domain.VisaCart;
import com.ruoyi.visa.service.IVisaCartService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 购物车Controller
 * 
 * @author bella
 * @date 2026-01-29
 */
@RestController
@RequestMapping("/visa/cart")
public class VisaCartController extends BaseController
{
    @Autowired
    private IVisaCartService visaCartService;

    /**
     * 查询购物车列表
     */
    @PreAuthorize("@ss.hasPermi('visa:cart:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaCart visaCart)
    {
        startPage();
        List<VisaCart> list = visaCartService.selectVisaCartList(visaCart);
        return getDataTable(list);
    }

    /**
     * 导出购物车列表
     */
    @PreAuthorize("@ss.hasPermi('visa:cart:export')")
    @Log(title = "购物车", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaCart visaCart)
    {
        List<VisaCart> list = visaCartService.selectVisaCartList(visaCart);
        ExcelUtil<VisaCart> util = new ExcelUtil<VisaCart>(VisaCart.class);
        util.exportExcel(response, list, "购物车数据");
    }

    /**
     * 获取购物车详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:cart:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaCartService.selectVisaCartById(id));
    }

    /**
     * 新增购物车
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody VisaCart visaCart) {
        VisaCart query = new VisaCart();
        query.setCustomerId(visaCart.getCustomerId());
        query.setProductId(visaCart.getProductId());
        List<VisaCart> list = visaCartService.selectVisaCartList(query);

        if (!list.isEmpty()) {
            VisaCart oldItem = list.get(0);
            // 严谨写法：处理数据库里旧数据为 null 的情况
            long currentQty = (oldItem.getQuantity() == null) ? 1L : oldItem.getQuantity();
            oldItem.setQuantity(currentQty + 1);

            // 这里会调用上面修改后的 XML 里的 update 语句
            return toAjax(visaCartService.updateVisaCart(oldItem));
        } else {
            visaCart.setQuantity(1L);
            return toAjax(visaCartService.insertVisaCart(visaCart));
        }
    }
    /**
     * 修改购物车
     */
    @PreAuthorize("@ss.hasPermi('visa:cart:edit')")
    @Log(title = "购物车", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaCart visaCart)
    {
        return toAjax(visaCartService.updateVisaCart(visaCart));
    }

    /**
     * 删除购物车
     */
    @PreAuthorize("@ss.hasPermi('visa:cart:remove')")
    @Log(title = "购物车", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaCartService.deleteVisaCartByIds(ids));
    }

    /**
     * 专门用于购物车页面修改数量的接口
     */
    @PutMapping("/updateQuantity")
    public AjaxResult updateQuantity(@RequestBody VisaCart visaCart) {
        return toAjax(visaCartService.updateVisaCart(visaCart));
    }
}
