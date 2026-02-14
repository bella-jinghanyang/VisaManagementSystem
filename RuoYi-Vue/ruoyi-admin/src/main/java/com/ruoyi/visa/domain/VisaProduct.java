package com.ruoyi.visa.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 签证产品对象 visa_product
 * 
 * @author bella
 * @date 2026-01-24
 */
public class VisaProduct extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键，自增ID */
    private Long id;

    /** 产品标题（如：美国B1/B2十年多次签） */
    @Excel(name = "产品标题", readConverterExp = "如=：美国B1/B2十年多次签")
    private String title;

    /** 关联国家ID */
    @Excel(name = "关联国家ID")
    private Long countryId;

    /** 关联类型ID */
    @Excel(name = "关联类型ID")
    private Long typeId;

    /** 销售价格（单位：元） */
    @Excel(name = "销售价格", readConverterExp = "单=位：元")
    private BigDecimal price;

    /** 产品封面图片路径 */
    @Excel(name = "产品封面图片路径")
    private String image;

    /** 核心字段：材料要求配置规则（JSON数组） */
    @Excel(name = "核心字段：材料要求配置规则", readConverterExp = "J=SON数组")
    private String requirementsConfig;

    /** 是否需要面签（0:否, 1:是） */
    @Excel(name = "是否需要面签", readConverterExp = "0=:否,,1=:是")
    private Integer isInterviewRequired;

    /** 预计办理时长（如：5-7个工作日） */
    @Excel(name = "预计办理时长", readConverterExp = "如=：5-7个工作日")
    private String processingTime;

    /** 有效期（如：10年） */
    @Excel(name = "有效期", readConverterExp = "如=：10年")
    private String validityPeriod;

    /** 最长停留天数（如：90天） */
    @Excel(name = "最长停留天数", readConverterExp = "如=：90天")
    private String maxStayDays;

    /** 关联领区ID */
    @Excel(name = "关联领区ID")
    private Long districtId;

    /** 上架状态（1:上架, 0:下架） */
    @Excel(name = "上架状态", readConverterExp = "1=:上架,,0=:下架")
    private Integer status;

    private String countryName;
    private String typeName;
    private String districtName;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setCountryId(Long countryId) 
    {
        this.countryId = countryId;
    }

    public Long getCountryId() 
    {
        return countryId;
    }

    public void setTypeId(Long typeId) 
    {
        this.typeId = typeId;
    }

    public Long getTypeId() 
    {
        return typeId;
    }

    public void setPrice(BigDecimal price) 
    {
        this.price = price;
    }

    public BigDecimal getPrice() 
    {
        return price;
    }

    public void setImage(String image) 
    {
        this.image = image;
    }

    public String getImage() 
    {
        return image;
    }

    public void setRequirementsConfig(String requirementsConfig) 
    {
        this.requirementsConfig = requirementsConfig;
    }

    public String getRequirementsConfig() 
    {
        return requirementsConfig;
    }

    public void setIsInterviewRequired(Integer isInterviewRequired) 
    {
        this.isInterviewRequired = isInterviewRequired;
    }

    public Integer getIsInterviewRequired() 
    {
        return isInterviewRequired;
    }

    public void setProcessingTime(String processingTime) 
    {
        this.processingTime = processingTime;
    }

    public String getProcessingTime() 
    {
        return processingTime;
    }

    public void setValidityPeriod(String validityPeriod) 
    {
        this.validityPeriod = validityPeriod;
    }

    public String getValidityPeriod() 
    {
        return validityPeriod;
    }

    public void setMaxStayDays(String maxStayDays) 
    {
        this.maxStayDays = maxStayDays;
    }

    public String getMaxStayDays() 
    {
        return maxStayDays;
    }

    public void setDistrictId(Long districtId) 
    {
        this.districtId = districtId;
    }

    public Long getDistrictId() 
    {
        return districtId;
    }

    public void setStatus(Integer status) 
    {
        this.status = status;
    }

    public Integer getStatus() 
    {
        return status;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("title", getTitle())
            .append("countryId", getCountryId())
            .append("typeId", getTypeId())
            .append("price", getPrice())
            .append("image", getImage())
            .append("requirementsConfig", getRequirementsConfig())
            .append("isInterviewRequired", getIsInterviewRequired())
            .append("processingTime", getProcessingTime())
            .append("validityPeriod", getValidityPeriod())
            .append("maxStayDays", getMaxStayDays())
            .append("districtId", getDistrictId())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
