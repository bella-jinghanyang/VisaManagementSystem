package com.ruoyi.client.controller;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.visa.domain.VisaCart;
import com.ruoyi.visa.domain.VisaOrder;
import com.ruoyi.visa.domain.VisaProduct;
import com.ruoyi.visa.service.IVisaCartService;
import com.ruoyi.visa.service.IVisaOrderService;
import com.ruoyi.visa.service.IVisaProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * C端订单接口
 */
@RestController
@RequestMapping("/client/order")
public class ClientOrderController extends BaseController {

    @Autowired
    private IVisaOrderService visaOrderService;

    @Autowired
    private IVisaProductService visaProductService;

    @Autowired
    private IVisaCartService visaCartService;

    /**
     * 提交订单（第一步：生成订单，不含材料上传）
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody VisaOrder order) {
        // 1. 基础校验
        if (order.getProductId() == null || order.getCustomerId() == null) {
            return error("下单失败，参数缺失");
        }

        // 2. 获取产品信息，用于计算总价和存快照
        VisaProduct product = visaProductService.selectVisaProductById(order.getProductId());
        if (product == null || product.getStatus() == 0) {
            return error("该签证产品已下架或不存在");
        }

        // 3. 补全订单号 (规则：时间戳 + 随机位)
        // 例如：202601291030 + UUID截取
        String orderNo = DateUtils.dateTimeNow() + IdUtils.fastSimpleUUID().substring(0, 6).toUpperCase();
        order.setOrderNo(orderNo);

        // 4. 计算总金额 (单价 * 数量)
        long qty = (order.getQuantity() == null || order.getQuantity() < 1) ? 1 : order.getQuantity();
        order.setQuantity(qty);
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(qty));
        order.setTotalAmount(totalAmount);

        // 5. 设置初始状态：0 (待支付)
        order.setStatus(0L);
        order.setCreateTime(DateUtils.getNowDate());

        // 6. 生成产品快照
        // 记录下单时的标题和单价，防止以后后台改价产生纠纷
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("title", product.getTitle());
        snapshot.put("unitPrice", product.getPrice());
        snapshot.put("image", product.getImage());
        order.setProductSnapshot(JSON.toJSONString(snapshot));

        // 7. 写入数据库
        // 注意：这里调用的应该是你生成的 Service 方法
        int rows = visaOrderService.insertVisaOrder(order);

        if (rows > 0) {
            // 8. 清空购物车中对应的产品
            VisaCart cartQuery = new VisaCart();
            cartQuery.setCustomerId(order.getCustomerId()); // 谁的购物车
            cartQuery.setProductId(order.getProductId());   // 哪个产品

            // 逻辑：根据用户ID和产品ID找到那条购物车记录并删除
            // 建议你直接在 service 里写个删除方法，或者先查出 ID 再删
            List<VisaCart> cartList = visaCartService.selectVisaCartList(cartQuery);
            for (VisaCart cartItem : cartList) {
                visaCartService.deleteVisaCartById(cartItem.getId());
            }

            // 返回订单号，前端拿到后可以去支付
            return AjaxResult.success("下单成功", orderNo);
        } else {
            return error("服务器繁忙，请稍后再试");
        }
    }

    /**
     * 获取当前用户的订单列表
     */
    @GetMapping("/list/{customerId}") // ★ 确保这里是 /list，不是 /myList
    public AjaxResult list(@PathVariable("customerId") Long customerId) {
        VisaOrder query = new VisaOrder();
        query.setCustomerId(customerId);
        // 按创建时间倒序（新订单在上面）
        // 这一行需要你的 XML 里支持 order by，如果报错可以先删掉 setOrderBy
        // query.getParams().put("orderBy", "create_time desc");

        List<VisaOrder> list = visaOrderService.selectVisaOrderList(query);
        return success(list);
    }

    /**
     * 获取当前用户待办订单数量 (只要不是状态6:已完成，都算待办)
     */
    @GetMapping("/countPending/{customerId}")
    public AjaxResult countPending(@PathVariable("customerId") Long customerId) {
        VisaOrder query = new VisaOrder();
        query.setCustomerId(customerId);
        List<VisaOrder> list = visaOrderService.selectVisaOrderList(query);

        // 逻辑：只要状态不是 6 (已完成)，就计入角标
        long count = list.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() != 6)
                .count();

        return success(count);
    }

    /**
     * 提交办签材料
     */
    @PostMapping("/submitMaterials")
    public AjaxResult submitMaterials(@RequestBody VisaOrder order) {
        // 1. 基础检查
        if (order.getId() == null || StringUtils.isEmpty(order.getSubmittedMaterials())) {
            return error("提交失败，资料不能为空");
        }

        // 2. 更新状态：从 1(待上传) 变为 2(待审核)
        order.setStatus(2L);
        order.setUpdateTime(DateUtils.getNowDate());

        // 3. 执行更新
        return toAjax(visaOrderService.updateVisaOrder(order));
    }

    /**
     * 获取订单详情
     * @param id 订单ID
     */
    @GetMapping("/detail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        // 调用你之前用代码生成器生成的 Service 方法
        VisaOrder order = visaOrderService.selectVisaOrderById(id);

        if (order == null) {
            return error("该订单不存在");
        }

        // 建议增加一个安全校验（可选）：防止 A 用户通过修改 URL 的 ID 查到 B 用户的订单
        // String token = ServletUtils.getRequest().getHeader("Client-Token");
        // 根据 token 校验 customerId 是否匹配... (毕设如果赶时间可以先不写)

        return success(order);
    }

    /**
     * 修改订单信息 (用于用户确认面试时间、确认收货等)
     */
    @PutMapping("/update")
    public AjaxResult update(@RequestBody VisaOrder order) {
        if (order.getId() == null) {
            return error("订单ID不能为空");
        }
        // 执行更新
        return toAjax(visaOrderService.updateVisaOrder(order));
    }

    /**
     * 提交补充材料 (针对 Check 情况)
     */
    @PutMapping("/submitSupplementary")
    public AjaxResult submitSupplementary(@RequestBody VisaOrder order) {
        if (order.getId() == null || order.getSupplementaryMaterials() == null) {
            return error("参数缺失");
        }
        // 逻辑：仅更新补充材料字段，不强制改状态（除非你想改回待审核）
        // 管理员会在后台通过“留言板”或刷新列表看到新材料
        return toAjax(visaOrderService.updateVisaOrder(order));
    }
}