package com.ruoyi.client.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.visa.domain.VisaComment;
import com.ruoyi.visa.domain.VisaOrder;
import com.ruoyi.visa.service.IVisaCommentService;
import com.ruoyi.visa.service.IVisaOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client/comment")
public class ClientCommentController extends BaseController {

    @Autowired
    private IVisaCommentService visaCommentService;

    @Autowired
    private IVisaOrderService orderService; // 注入订单服务

    /**
     * 获取某个产品的评价列表 (匿名可看)
     */
    @Anonymous
    @GetMapping("/list/{productId}")
    public AjaxResult list(@PathVariable("productId") Long productId) {
        VisaComment query = new VisaComment();
        query.setProductId(productId);
        // 调用我们刚才改好的联表查询方法
        return success(visaCommentService.selectVisaCommentList(query));
    }

    /**
     * 提交评价 (必须登录)
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody VisaComment comment) {
        // 处理 JSON 字段的空字符串问题
        // 如果 images 是空的或者是 "[]"，直接设为 null
        if (StringUtils.isEmpty(comment.getImages()) || "[]".equals(comment.getImages())) {
            comment.setImages(null);
        }

        int rows = visaCommentService.insertVisaComment(comment);
        if (rows > 0) {
            // 更新订单评价状态逻辑保持不变
            VisaOrder order = new VisaOrder();
            order.setId(comment.getOrderId());
            order.setIsCommented("1");
            orderService.updateVisaOrder(order);
        }
        return toAjax(rows);
    }

    /**
     * 追加评价
     */
    @PutMapping("/addAdditional")
    public AjaxResult addAdditional(@RequestBody VisaComment comment) {
        if (comment.getId() == null || StringUtils.isEmpty(comment.getAdditionalContent())) {
            return error("参数错误");
        }

        // 1. 设置追加时间为当前时间
        comment.setAdditionalTime(com.ruoyi.common.utils.DateUtils.getNowDate());

        // 2. 执行更新 (调用若依生成的 update 方法即可)
        // 这里底层 SQL 会根据 ID 更新 additional_content 和 additional_time
        return toAjax(visaCommentService.updateVisaComment(comment));
    }
}
