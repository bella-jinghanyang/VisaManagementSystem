package com.ruoyi.visa.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.utils.DateUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 签证评价对象 visa_comment
 * 
 * @author bella
 * @date 2026-02-20
 */
public class VisaComment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评价ID */
    private Long id;

    /** 关联订单ID */
    @Excel(name = "关联订单ID")
    private Long orderId;

    /** 关联产品ID */
    @Excel(name = "关联产品ID")
    private Long productId;

    /** 关联客户ID */
    @Excel(name = "关联客户ID")
    private Long customerId;

    /** 评分 */
    @Excel(name = "评分")
    private Integer rating;

    /** 评价内容 */
    @Excel(name = "评价内容")
    private String content;

    /** 晒图 */
    @Excel(name = "晒图")
    private String images;

    /** 管理员回复 */
    @Excel(name = "管理员回复")
    private String adminReply;

    /** 回复时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "回复时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date replyTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "评价时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createTime;

    private String customerNickname; // 客户昵称
    private String customerAvatar;   // 客户头像

    private String additionalContent; // 追加内容

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date additionalTime;      // 追加时间

    private String orderNo;

    public String getOrderNo() {
        return orderNo;
    }
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getAdditionalContent() {
        return additionalContent;
    }

    public void setAdditionalContent(String additionalContent) {
        this.additionalContent = additionalContent;
    }

    public Date getAdditionalTime() {
        return additionalTime;
    }

    public void setAdditionalTime(Date additionalTime) {
        this.additionalTime = additionalTime;
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

    public void setProductId(Long productId) 
    {
        this.productId = productId;
    }

    public Long getProductId() 
    {
        return productId;
    }

    public void setCustomerId(Long customerId) 
    {
        this.customerId = customerId;
    }

    public Long getCustomerId() 
    {
        return customerId;
    }

    public void setRating(Integer rating) 
    {
        this.rating = rating;
    }

    public Integer getRating() 
    {
        return rating;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setImages(String images) 
    {
        this.images = images;
    }

    public String getImages() 
    {
        return images;
    }

    public void setAdminReply(String adminReply) 
    {
        this.adminReply = adminReply;
    }

    public String getAdminReply() 
    {
        return adminReply;
    }

    public void setReplyTime(Date replyTime) 
    {
        this.replyTime = replyTime;
    }

    public Date getReplyTime() 
    {
        return replyTime;
    }

    public String getCustomerNickname() {
        return customerNickname;
    }

    public void setCustomerNickname(String customerNickname) {
        this.customerNickname = customerNickname;
    }

    public String getCustomerAvatar() {
        return customerAvatar;
    }

    public void setCustomerAvatar(String customerAvatar) {
        this.customerAvatar = customerAvatar;
    }
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderId", getOrderId())
            .append("productId", getProductId())
            .append("customerId", getCustomerId())
            .append("rating", getRating())
            .append("content", getContent())
            .append("images", getImages())
            .append("adminReply", getAdminReply())
            .append("replyTime", getReplyTime())
            .append("createTime", getCreateTime())
            .toString();
    }
}
