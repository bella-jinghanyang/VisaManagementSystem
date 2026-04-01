package com.ruoyi.visa.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.visa.service.IVisaOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visa/index")
public class VisaIndexController extends BaseController {

    @Autowired
    private IVisaOrderService visaOrderService;

    @GetMapping("/data")
    public AjaxResult getIndexData() {
        return success(visaOrderService.getIndexData());
    }
}