package com.ruoyi.visa.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 国家配置对象 visa_country
 * 
 * @author ruoyi
 * @date 2026-01-23
 */
public class VisaCountry extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 国家名称(如:日本) */
    @Excel(name = "国家名称(如:日本)")
    private String name;

    /** 国家代码(如:JP) */
    @Excel(name = "国家代码(如:JP)")
    private String code;

    /** 所属大洲(如:亚洲) */
    @Excel(name = "所属大洲(如:亚洲)")
    private String continent;

    /** 国旗图片(可选) */
    @Excel(name = "国旗图片(可选)")
    private String flagUrl;

    /** 排序 */
    @Excel(name = "排序")
    private Long sort;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态(0正常 1停用)")
    private String status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setCode(String code) 
    {
        this.code = code;
    }

    public String getCode() 
    {
        return code;
    }

    public void setContinent(String continent) 
    {
        this.continent = continent;
    }

    public String getContinent() 
    {
        return continent;
    }

    public void setFlagUrl(String flagUrl) 
    {
        this.flagUrl = flagUrl;
    }

    public String getFlagUrl() 
    {
        return flagUrl;
    }

    public void setSort(Long sort) 
    {
        this.sort = sort;
    }

    public Long getSort() 
    {
        return sort;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("code", getCode())
            .append("continent", getContinent())
            .append("flagUrl", getFlagUrl())
            .append("sort", getSort())
            .append("status", getStatus())
            .toString();
    }
}
