package com.ruoyi.client.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.visa.domain.VisaCountry;
import com.ruoyi.visa.domain.VisaProduct;
import com.ruoyi.visa.domain.VisaType;
import com.ruoyi.visa.service.IVisaCountryService;
import com.ruoyi.visa.service.IVisaProductService;
import com.ruoyi.visa.service.IVisaTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/client/product")
public class ClientProductController extends BaseController {
    @Autowired
    private IVisaProductService productService;

    @Autowired
    private IVisaCountryService countryService;

    @Autowired
    private IVisaTypeService typeService;

    /**
     * 获取所有上架的国家
     */
    @Anonymous // ★ 允许匿名访问
    @GetMapping("/country")
    public AjaxResult country() {
        VisaCountry query = new VisaCountry();
        query.setStatus("1"); // 只查上架的
        List<VisaCountry> list = countryService.selectVisaCountryList(query);
        return success(list);
    }

    /**
     * 获取所有上架的签证类型
     */
    @Anonymous
    @GetMapping("/type")
    public AjaxResult type() {
        VisaType query = new VisaType();
        query.setStatus("1"); // 只查上架的
        List<VisaType> list = typeService.selectVisaTypeList(query);
        return success(list);
    }

    /**
     * 查询上架的签证产品列表
     */
//    @Anonymous // 允许C端用户不登录直接访问
//    @GetMapping("/list")
//    public AjaxResult list(VisaProduct product) {
//        product.setStatus(1); // 强制只查上架状态(1)的产品
//        List<VisaProduct> list = productService.selectVisaProductList(product);
//        return success(list);
//    }

    /**
     * 查询产品详情
     */
    @Anonymous
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(productService.selectVisaProductById(id));
    }


    /**
     * 查询上架的签证产品列表（分页功能）
     */
    @Anonymous
    @GetMapping("/list")
// 1. 返回类型改为 TableDataInfo (它包含 rows 和 total)
    public TableDataInfo list(VisaProduct product) {
        // 2. 开启分页 (自动读取前端传的 pageNum 和 pageSize)
        startPage();

        product.setStatus(1);
        List<VisaProduct> list = productService.selectVisaProductList(product);

        // 3. 包装成分页对象返回
        return getDataTable(list);
    }
}
