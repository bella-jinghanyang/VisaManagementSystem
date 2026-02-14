package com.ruoyi.client.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.visa.domain.CCustomer;
import com.ruoyi.visa.service.ICCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * C端客户认证接口
 */
@RestController
@RequestMapping("/client/auth")
public class ClientAuthController extends BaseController {

    @Autowired
    private ICCustomerService customerService;

    /**
     * 注册
     */
    @Anonymous // 允许匿名访问
    @PostMapping("/register")
    public AjaxResult register(@RequestBody CCustomer customer) {
        // 1. 基础校验
        if (StringUtils.isEmpty(customer.getPhone()) || StringUtils.isEmpty(customer.getPassword())) {
            return error("手机号和密码不能为空");
        }

        // 2. 检查手机号是否已存在
        CCustomer query = new CCustomer();
        query.setPhone(customer.getPhone());
        List<CCustomer> list = customerService.selectCCustomerList(query);
        if (list.size() > 0) {
            return error("注册失败，该手机号已存在");
        }

        // 3. 密码加密 (使用若依自带的 BCrypt 加密工具)
        customer.setPassword(SecurityUtils.encryptPassword(customer.getPassword()));

        // 4. 设置默认值 (昵称默认为手机号)
        if (StringUtils.isEmpty(customer.getNickname())) {
            customer.setNickname("用户" + customer.getPhone().substring(7));
        }

        return toAjax(customerService.insertCCustomer(customer));
    }

    /**
     * 登录
     */
    @Anonymous // 允许匿名访问
    @PostMapping("/login")
    public AjaxResult login(@RequestBody CCustomer loginBody) {
        String phone = loginBody.getPhone();
        String password = loginBody.getPassword();

        // 1. 查找用户
        CCustomer query = new CCustomer();
        query.setPhone(phone);
        List<CCustomer> list = customerService.selectCCustomerList(query);

        if (list.isEmpty()) {
            return error("登录失败：用户不存在");
        }

        CCustomer user = list.get(0);

        // 2. 校验密码
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            return error("登录失败：密码错误");
        }

        // 3. 校验状态
        if ("1".equals(user.getStatus())) { // 假设1是停用
            return error("对不起，您的账号已被封禁");
        }

        user.setLoginIp(com.ruoyi.common.utils.ip.IpUtils.getIpAddr(com.ruoyi.common.utils.ServletUtils.getRequest()));
        user.setLoginDate(com.ruoyi.common.utils.DateUtils.getNowDate());
        customerService.updateCCustomer(user);

        // 4. 生成简单 Token (毕设偷懒做法：用 UUID 当 Token)
        // 实际项目应该存 Redis，这里为了简单直接返回
        String token = UUID.randomUUID().toString();

        // 5. 返回结果 (把用户信息也返回去，前端好存 ID)
        AjaxResult ajax = AjaxResult.success("登录成功");
        ajax.put("token", token);
        ajax.put("user", user);
        return ajax;
    }
}