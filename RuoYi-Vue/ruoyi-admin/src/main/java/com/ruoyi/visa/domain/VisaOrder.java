package com.ruoyi.visa.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 签证订单对象 visa_order
 * 
 * @author bella
 * @date 2026-01-30
 */
public class VisaOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long id;

    /** 订单编号(唯一) */
    @Excel(name = "订单编号(唯一)")
    private String orderNo;

    /** 客户ID(关联c_customer) */
    @Excel(name = "客户ID(关联c_customer)")
    private Long customerId;

    /** 产品ID(关联visa_product) */
    @Excel(name = "产品ID(关联visa_product)")
    private Long productId;

    /** 订单数量 */
    @Excel(name = "订单数量")
    private Long quantity;

    /** 产品信息快照(存下单时的标题和价格) */
    @Excel(name = "产品信息快照(存下单时的标题和价格)")
    private String productSnapshot;

    /** 订单总金额 */
    @Excel(name = "订单总金额")
    private BigDecimal totalAmount;

    /** 订单状态(0待支付 1待上传 2待审核 3待补交 4办理中 5待收货 6已完成 7待面试) */
    @Excel(name = "订单状态(0待支付 1待上传 2待审核 3待补交 4办理中 5待收货 6已完成 7待面试)")
    private Long status;

    /** 支付宝交易流水号 */
    @Excel(name = "支付宝交易流水号")
    private String alipayNo;

    /** 支付成功时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "支付成功时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 用户提交的动态材料数据(JSON) */
    @Excel(name = "用户提交的动态材料数据(JSON)")
    private String submittedMaterials;

    /** 审核备注/驳回原因 */
    @Excel(name = "审核备注/驳回原因")
    private String auditRemark;

    /** 预约面试时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "预约面试时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date interviewTime;

    /** 面试预约单文件路径 */
    @Excel(name = "面试预约单文件路径")
    private String interviewFile;

    /** 面试反馈(0未反馈 1过签 2拒签 3Check) */
//    @Excel(name = "面试反馈(0未反馈 1过签 2拒签 3Check)")
    private String interviewFeedback;

    /** 最终签证结果(1出签 2拒签) */
//    @Excel(name = "最终签证结果(1出签 2拒签)")
    private String visaResult;


    /** 收货地址快照(收件人,电话,地址) */
    @Excel(name = "收货地址快照(收件人,电话,地址)")
    private String mailingAddress;

    /** 删除标志(0代表存在 2代表删除) */
    private String delFlag;

    /** 是否需要面签 (0否 1是) - 非数据库字段，来自产品表 */
    private Integer isInterviewRequired;

    /** 可选面试时间 */
    private String interviewSlots;

    /** check补交材料 */
    private String supplementaryMaterials;

    private String isCommented;

    private Long commentId;
    private Integer commentRate;
    private String commentText;
    private String adminReply;
    private String additionalContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date additionalTime;

    private String isAi;

    private List<VisaOrderApplicant> applicantList;

    private VisaOrderLogistics logistics;
    private Integer isPhysicalRequired;
    private String expressToAgency;
    private String trackingNumber;
    private String courierCompany;
    private String userRemark;

    public void setUserRemark(String userRemark) {
        this.userRemark = userRemark;
    }
    public String getUserRemark() {
        return userRemark;
    }

    public String getCourierCompany() { return courierCompany; }
    public void setCourierCompany(String courierCompany) { this.courierCompany = courierCompany; }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getExpressToAgency() {
        return expressToAgency;
    }
    public void setExpressToAgency(String expressToAgency) {
        this.expressToAgency = expressToAgency;
    }

    public Integer getIsPhysicalRequired() {
        return isPhysicalRequired;
    }

    public void setIsPhysicalRequired(Integer isPhysicalRequired) {
        this.isPhysicalRequired = isPhysicalRequired;
    }

    public VisaOrderLogistics getLogistics() {
        return logistics;
    }
    public void setLogistics(VisaOrderLogistics logistics) {
        this.logistics = logistics;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    private String identity;

    public List<VisaOrderApplicant> getApplicantList() {
        return applicantList;
    }

    public void setApplicantList(List<VisaOrderApplicant> applicantList) {
        this.applicantList = applicantList;
    }

    public String getIsAi() {
        return isAi;
    }

    public void setIsAi(String isAi) {
        this.isAi = isAi;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setOrderNo(String orderNo) 
    {
        this.orderNo = orderNo;
    }

    public String getOrderNo() 
    {
        return orderNo;
    }

    public void setCustomerId(Long customerId) 
    {
        this.customerId = customerId;
    }

    public Long getCustomerId() 
    {
        return customerId;
    }

    public void setProductId(Long productId) 
    {
        this.productId = productId;
    }

    public Long getProductId() 
    {
        return productId;
    }

    public void setQuantity(Long quantity) 
    {
        this.quantity = quantity;
    }

    public Long getQuantity() 
    {
        return quantity;
    }

    public void setProductSnapshot(String productSnapshot) 
    {
        this.productSnapshot = productSnapshot;
    }

    public String getProductSnapshot() 
    {
        return productSnapshot;
    }

    public void setTotalAmount(BigDecimal totalAmount) 
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount() 
    {
        return totalAmount;
    }

    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
    {
        return status;
    }

    public void setAlipayNo(String alipayNo) 
    {
        this.alipayNo = alipayNo;
    }

    public String getAlipayNo() 
    {
        return alipayNo;
    }

    public void setPayTime(Date payTime) 
    {
        this.payTime = payTime;
    }

    public Date getPayTime() 
    {
        return payTime;
    }

    public void setSubmittedMaterials(String submittedMaterials) 
    {
        this.submittedMaterials = submittedMaterials;
    }

    public String getSubmittedMaterials() 
    {
        return submittedMaterials;
    }

    public void setAuditRemark(String auditRemark) 
    {
        this.auditRemark = auditRemark;
    }

    public String getAuditRemark() 
    {
        return auditRemark;
    }

    public void setInterviewTime(Date interviewTime) 
    {
        this.interviewTime = interviewTime;
    }

    public Date getInterviewTime() 
    {
        return interviewTime;
    }

    public void setInterviewFile(String interviewFile) 
    {
        this.interviewFile = interviewFile;
    }

    public String getInterviewFile() 
    {
        return interviewFile;
    }

    public void setInterviewFeedback(String interviewFeedback)
    {
        this.interviewFeedback = interviewFeedback;
    }

    public String getInterviewFeedback()
    {
        return interviewFeedback;
    }

    public void setVisaResult(String visaResult)
    {
        this.visaResult = visaResult;
    }

    public String getVisaResult()
    {
        return visaResult;
    }

    public void setMailingAddress(String mailingAddress) 
    {
        this.mailingAddress = mailingAddress;
    }

    public String getMailingAddress() 
    {
        return mailingAddress;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public Integer getIsInterviewRequired() {
        return isInterviewRequired;
    }

    public void setIsInterviewRequired(Integer isInterviewRequired) {
        this.isInterviewRequired = isInterviewRequired;
    }

    public String getInterviewSlots() {
        return interviewSlots;
    }

    public void setInterviewSlots(String interviewSlots) {
        this.interviewSlots = interviewSlots;
    }

    public String getSupplementaryMaterials() {
        return supplementaryMaterials;
    }

    public void setSupplementaryMaterials(String supplementaryMaterials) {
        this.supplementaryMaterials = supplementaryMaterials;
    }



    public String getIsCommented() {
        return isCommented;
    }

    public void setIsCommented(String isCommented) {
        this.isCommented = isCommented;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Integer getCommentRate() {
        return commentRate;
    }

    public void setCommentRate(Integer commentRate) {
        this.commentRate = commentRate;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderNo", getOrderNo())
            .append("customerId", getCustomerId())
            .append("productId", getProductId())
            .append("quantity", getQuantity())
            .append("productSnapshot", getProductSnapshot())
            .append("totalAmount", getTotalAmount())
            .append("status", getStatus())
            .append("alipayNo", getAlipayNo())
            .append("payTime", getPayTime())
            .append("submittedMaterials", getSubmittedMaterials())
            .append("auditRemark", getAuditRemark())
            .append("interviewTime", getInterviewTime())
            .append("interviewFile", getInterviewFile())
            .append("interviewFeedback", getInterviewFeedback())
            .append("visaResult", getVisaResult())
            .append("mailingAddress", getMailingAddress())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())

            .toString();
    }


}
