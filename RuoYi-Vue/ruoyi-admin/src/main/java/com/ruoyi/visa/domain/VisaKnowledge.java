package com.ruoyi.visa.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 签证知识库对象 visa_knowledge
 * 
 * @author bella
 * @date 2026-02-17
 */
public class VisaKnowledge extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 分类 */
    @Excel(name = "分类")
    private String category;

    /** 知识点标题 */
    @Excel(name = "知识点标题")
    private String title;

    /** 搜索关键词 */
    @Excel(name = "搜索关键词")
    private String keywords;

    /** 详细内容 */
    @Excel(name = "详细内容")
    private String content;

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

    public void setCategory(String category) 
    {
        this.category = category;
    }

    public String getCategory() 
    {
        return category;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setKeywords(String keywords) 
    {
        this.keywords = keywords;
    }

    public String getKeywords() 
    {
        return keywords;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
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
            .append("category", getCategory())
            .append("title", getTitle())
            .append("keywords", getKeywords())
            .append("content", getContent())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
