package com.ruoyi.visa.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单留言记录对象 order_message
 * 
 * @author bella
 * @date 2026-03-09
 */
public class OrderMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long id;

    /** 关联订单ID(0为通用咨询) */
    @Excel(name = "关联订单ID(0为通用咨询)")
    private Long orderId;

    /** 发送者类型(1:客户 2:管理员) */
    @Excel(name = "发送者类型(1:客户 2:管理员)")
    private Integer senderType;

    /** 消息内容 */
    @Excel(name = "消息内容")
    private String content;

    /** 图片路径(可选) */
    @Excel(name = "图片路径(可选)")
    private String imageUrl;

    private Long customerId;

    private String isAi;

    private String orderNo;

    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }


    public String getIsAi() {
        return isAi;
    }

    public void setIsAi(String isAi) {
        this.isAi = isAi;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
    }

    public void setSenderType(Integer senderType) 
    {
        this.senderType = senderType;
    }

    public Integer getSenderType() 
    {
        return senderType;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderId", getOrderId())
            .append("senderType", getSenderType())
            .append("content", getContent())
            .append("imageUrl", getImageUrl())
            .append("createTime", getCreateTime())
            .toString();
    }
}
