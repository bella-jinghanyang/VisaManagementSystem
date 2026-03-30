package com.ruoyi.visa.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单物流信息对象 visa_order_logistics
 * 
 * @author bella
 * @date 2026-03-29
 */
public class VisaOrderLogistics extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 关联订单ID */
    @Excel(name = "关联订单ID")
    private Long orderId;

    /** 关联订单号 */
    @Excel(name = "关联订单号")
    private String orderNo;

    /** 方向: 1-用户寄给中介, 2-中介寄回用户 */
    @Excel(name = "方向: 1-用户寄给中介, 2-中介寄回用户")
    private Integer direction;

    /** 快递公司(顺丰/EMS等) */
    @Excel(name = "快递公司(顺丰/EMS等)")
    private String courierCompany;

    /** 快递单号 */
    @Excel(name = "快递单号")
    private String trackingNo;

    /** 寄件人姓名 */
    @Excel(name = "寄件人姓名")
    private String senderName;

    /** 寄件人电话 */
    @Excel(name = "寄件人电话")
    private String senderPhone;

    /** 详细邮寄地址快照 */
    @Excel(name = "详细邮寄地址快照")
    private String mailAddress;

    /** 状态: 0-待寄出, 1-已寄出, 2-已签收 */
    @Excel(name = "状态: 0-待寄出, 1-已寄出, 2-已签收")
    private Integer status;

    /** 签收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签收时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date receiveTime;

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

    public void setOrderNo(String orderNo) 
    {
        this.orderNo = orderNo;
    }

    public String getOrderNo() 
    {
        return orderNo;
    }

    public void setDirection(Integer direction) 
    {
        this.direction = direction;
    }

    public Integer getDirection() 
    {
        return direction;
    }

    public void setCourierCompany(String courierCompany) 
    {
        this.courierCompany = courierCompany;
    }

    public String getCourierCompany() 
    {
        return courierCompany;
    }

    public void setTrackingNo(String trackingNo) 
    {
        this.trackingNo = trackingNo;
    }

    public String getTrackingNo() 
    {
        return trackingNo;
    }

    public void setSenderName(String senderName) 
    {
        this.senderName = senderName;
    }

    public String getSenderName() 
    {
        return senderName;
    }

    public void setSenderPhone(String senderPhone) 
    {
        this.senderPhone = senderPhone;
    }

    public String getSenderPhone() 
    {
        return senderPhone;
    }

    public void setMailAddress(String mailAddress) 
    {
        this.mailAddress = mailAddress;
    }

    public String getMailAddress() 
    {
        return mailAddress;
    }

    public void setStatus(Integer status) 
    {
        this.status = status;
    }

    public Integer getStatus() 
    {
        return status;
    }

    public void setReceiveTime(Date receiveTime) 
    {
        this.receiveTime = receiveTime;
    }

    public Date getReceiveTime() 
    {
        return receiveTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("direction", getDirection())
            .append("courierCompany", getCourierCompany())
            .append("trackingNo", getTrackingNo())
            .append("senderName", getSenderName())
            .append("senderPhone", getSenderPhone())
            .append("mailAddress", getMailAddress())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("receiveTime", getReceiveTime())
            .toString();
    }
}
