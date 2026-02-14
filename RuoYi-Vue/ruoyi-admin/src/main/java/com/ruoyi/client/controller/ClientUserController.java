package com.ruoyi.client.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.visa.domain.CCustomer;
import com.ruoyi.visa.service.ICCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * C端个人中心接口
 */
@RestController
@RequestMapping("/client/user")
public class ClientUserController extends BaseController {
    @Autowired
    private ICCustomerService customerService;

    /**
     * 获取个人信息
     * @param id 用户ID (从前端传过来)
     */
    @Anonymous
    @GetMapping("/profile/{id}")
    public AjaxResult profile(@PathVariable("id") Long id) {
        return success(customerService.selectCCustomerById(id));
    }

    /**
     * 修改个人信息
     */
    @Anonymous
    @PutMapping("/profile")
    public AjaxResult updateProfile(@RequestBody CCustomer customer) {
        // 简单校验
        if (customer.getId() == null) {
            return error("用户ID不能为空");
        }
        // 执行更新
        return toAjax(customerService.updateCCustomer(customer));
    }
}
