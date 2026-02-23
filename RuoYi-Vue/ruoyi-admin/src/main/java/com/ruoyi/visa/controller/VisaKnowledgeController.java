package com.ruoyi.visa.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.visa.domain.VisaKnowledge;
import com.ruoyi.visa.service.IVisaKnowledgeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 签证知识库Controller
 * 
 * @author bella
 * @date 2026-02-17
 */
@RestController
@RequestMapping("/visa/knowledge")
public class VisaKnowledgeController extends BaseController
{
    @Autowired
    private IVisaKnowledgeService visaKnowledgeService;

    /**
     * 查询签证知识库列表
     */
    @PreAuthorize("@ss.hasPermi('visa:knowledge:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaKnowledge visaKnowledge)
    {
        startPage();
        List<VisaKnowledge> list = visaKnowledgeService.selectVisaKnowledgeList(visaKnowledge);
        return getDataTable(list);
    }

    /**
     * 导出签证知识库列表
     */
    @PreAuthorize("@ss.hasPermi('visa:knowledge:export')")
    @Log(title = "签证知识库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaKnowledge visaKnowledge)
    {
        List<VisaKnowledge> list = visaKnowledgeService.selectVisaKnowledgeList(visaKnowledge);
        ExcelUtil<VisaKnowledge> util = new ExcelUtil<VisaKnowledge>(VisaKnowledge.class);
        util.exportExcel(response, list, "签证知识库数据");
    }

    /**
     * 获取签证知识库详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:knowledge:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaKnowledgeService.selectVisaKnowledgeById(id));
    }

    /**
     * 新增签证知识库
     */
    @PreAuthorize("@ss.hasPermi('visa:knowledge:add')")
    @Log(title = "签证知识库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaKnowledge visaKnowledge)
    {
        return toAjax(visaKnowledgeService.insertVisaKnowledge(visaKnowledge));
    }

    /**
     * 修改签证知识库
     */
    @PreAuthorize("@ss.hasPermi('visa:knowledge:edit')")
    @Log(title = "签证知识库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaKnowledge visaKnowledge)
    {
        return toAjax(visaKnowledgeService.updateVisaKnowledge(visaKnowledge));
    }

    /**
     * 删除签证知识库
     */
    @PreAuthorize("@ss.hasPermi('visa:knowledge:remove')")
    @Log(title = "签证知识库", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaKnowledgeService.deleteVisaKnowledgeByIds(ids));
    }
}
