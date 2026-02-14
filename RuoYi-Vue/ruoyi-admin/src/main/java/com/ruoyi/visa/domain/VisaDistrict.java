package com.ruoyi.visa.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 领区配置对象 visa_district
 * 
 * @author bella
 * @date 2026-01-23
 */
public class VisaDistrict extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 领区名称(如:北京领区) */
    @Excel(name = "领区名称(如:北京领区)")
    private String name;

    /** 覆盖省份(如:京,津,冀) */
    @Excel(name = "覆盖省份(如:京,津,冀)")
    private String coverArea;

    /** 排序 */
    @Excel(name = "排序")
    private Long sort;

    /** 状态 */
    @Excel(name = "状态")
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

    public void setCoverArea(String coverArea) 
    {
        this.coverArea = coverArea;
    }

    public String getCoverArea() 
    {
        return coverArea;
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
            .append("coverArea", getCoverArea())
            .append("sort", getSort())
            .append("status", getStatus())
            .toString();
    }
}
