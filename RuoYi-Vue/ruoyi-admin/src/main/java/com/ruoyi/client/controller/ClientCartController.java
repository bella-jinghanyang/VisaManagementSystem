package com.ruoyi.client.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.visa.domain.VisaCart;
import com.ruoyi.visa.service.IVisaCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client/cart")
public class ClientCartController extends BaseController {

    @Autowired
    private IVisaCartService visaCartService;

    /**
     * 加入预选清单 (购物车)
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody VisaCart visaCart) {
        // 1. 先查
        VisaCart query = new VisaCart();
        query.setCustomerId(visaCart.getCustomerId());
        query.setProductId(visaCart.getProductId());
        List<VisaCart> list = visaCartService.selectVisaCartList(query);

        if (!list.isEmpty()) {
            // 2. 存在则 +1
            VisaCart oldItem = list.get(0);
            // 如果数据库里数量是 null，给个默认值 1，否则直接 +1
            long newQty = (oldItem.getQuantity() == null ? 1 : oldItem.getQuantity()) + 1;
            oldItem.setQuantity(newQty);
            return toAjax(visaCartService.updateVisaCart(oldItem));
        } else {
            // 3. 不存在则插入新纪录，数量设为 1
            visaCart.setQuantity(1L);
            return toAjax(visaCartService.insertVisaCart(visaCart));
        }
    }

    /**
     * 获取当前用户的预选清单列表
     */
    @GetMapping("/list/{customerId}")
    public AjaxResult list(@PathVariable("customerId") Long customerId) {
        VisaCart query = new VisaCart();
        query.setCustomerId(customerId);
        List<VisaCart> list = visaCartService.selectVisaCartList(query);
        return success(list);
    }

    /**
     * 获取清单里的产品数量 (用于导航栏角标)
     */
    @GetMapping("/count/{customerId}")
    public AjaxResult count(@PathVariable("customerId") Long customerId) {
        VisaCart query = new VisaCart();
        query.setCustomerId(customerId);
        List<VisaCart> list = visaCartService.selectVisaCartList(query);
        return success(list.size());
    }

    /**
     * 从清单中移除
     */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(visaCartService.deleteVisaCartById(id));
    }

    /**
     * 专门用于购物车页面修改数量 (解决 PUT 405 报错)
     */
    @PutMapping("/updateQuantity")
    public AjaxResult updateQuantity(@RequestBody VisaCart visaCart) {
        // 这里前端传过来的是 {id: xx, quantity: xx}
        return toAjax(visaCartService.updateVisaCart(visaCart));
    }
}