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
import com.ruoyi.visa.domain.VisaComment;
import com.ruoyi.visa.service.IVisaCommentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 签证评价Controller
 * 
 * @author bella
 * @date 2026-02-20
 */
@RestController
@RequestMapping("/visa/comment")
public class VisaCommentController extends BaseController
{
    @Autowired
    private IVisaCommentService visaCommentService;


    /**
     * 查询签证评价列表
     */
    @PreAuthorize("@ss.hasPermi('visa:comment:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaComment visaComment)
    {
        startPage();
        List<VisaComment> list = visaCommentService.selectVisaCommentList(visaComment);
        return getDataTable(list);
    }

    /**
     * 导出签证评价列表
     */
    @PreAuthorize("@ss.hasPermi('visa:comment:export')")
    @Log(title = "签证评价", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaComment visaComment)
    {
        List<VisaComment> list = visaCommentService.selectVisaCommentList(visaComment);
        ExcelUtil<VisaComment> util = new ExcelUtil<VisaComment>(VisaComment.class);
        util.exportExcel(response, list, "签证评价数据");
    }

    /**
     * 获取签证评价详细信息
     */
    @PreAuthorize("@ss.hasPermi('visa:comment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaCommentService.selectVisaCommentById(id));
    }

    /**
     * 新增签证评价
     */
    @PreAuthorize("@ss.hasPermi('visa:comment:add')")
    @Log(title = "签证评价", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaComment visaComment)
    {
        return toAjax(visaCommentService.insertVisaComment(visaComment));
    }

    /**
     * 修改签证评价
     */
    @PreAuthorize("@ss.hasPermi('visa:comment:edit')")
    @Log(title = "签证评价", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaComment visaComment)
    {
        return toAjax(visaCommentService.updateVisaComment(visaComment));
    }

    /**
     * 删除签证评价
     */
    @PreAuthorize("@ss.hasPermi('visa:comment:remove')")
    @Log(title = "签证评价", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaCommentService.deleteVisaCommentByIds(ids));
    }
}
