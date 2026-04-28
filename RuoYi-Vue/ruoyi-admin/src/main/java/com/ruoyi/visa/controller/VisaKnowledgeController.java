package com.ruoyi.visa.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.annotation.Anonymous;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.visa.domain.VisaKnowledge;
import com.ruoyi.visa.service.IVisaKnowledgeService;
import com.ruoyi.visa.service.DocumentIngestionService;
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

    @Autowired
    private DocumentIngestionService documentIngestionService;

    @PreAuthorize("@ss.hasPermi('visa:knowledge:list')")
    @GetMapping("/list")
    public TableDataInfo list(VisaKnowledge visaKnowledge)
    {
        startPage();
        List<VisaKnowledge> list = visaKnowledgeService.selectVisaKnowledgeList(visaKnowledge);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('visa:knowledge:export')")
    @Log(title = "签证知识库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, VisaKnowledge visaKnowledge)
    {
        List<VisaKnowledge> list = visaKnowledgeService.selectVisaKnowledgeList(visaKnowledge);
        ExcelUtil<VisaKnowledge> util = new ExcelUtil<>(VisaKnowledge.class);
        util.exportExcel(response, list, "签证知识库数据");
    }

    @PreAuthorize("@ss.hasPermi('visa:knowledge:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(visaKnowledgeService.selectVisaKnowledgeById(id));
    }

    @PreAuthorize("@ss.hasPermi('visa:knowledge:add')")
    @Log(title = "签证知识库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody VisaKnowledge visaKnowledge)
    {
        return toAjax(visaKnowledgeService.insertVisaKnowledge(visaKnowledge));
    }

    @PreAuthorize("@ss.hasPermi('visa:knowledge:edit')")
    @Log(title = "签证知识库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody VisaKnowledge visaKnowledge)
    {
        return toAjax(visaKnowledgeService.updateVisaKnowledge(visaKnowledge));
    }

    @PreAuthorize("@ss.hasPermi('visa:knowledge:remove')")
    @Log(title = "签证知识库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(visaKnowledgeService.deleteVisaKnowledgeByIds(ids));
    }

    /**
     * 批量刷新知识库的语义向量。
     * 在首次部署、批量导入数据或更换 Embedding 模型后调用此接口完成初始化。
     */
    @Anonymous
    @Log(title = "签证知识库", businessType = BusinessType.UPDATE)
    @PostMapping("/refresh_all")
    public AjaxResult refreshEmbeddings()
    {
        int count = visaKnowledgeService.refreshAllEmbeddings();
        return AjaxResult.success("向量刷新完成，共更新 " + count + " 条知识条目");
    }

    /**
     * 上传原始文档（PDF / Word / Excel 等）并自动完成知识摄取。
     *
     * <p>完整流程：</p>
     * <ol>
     *   <li>在 MySQL 中创建知识条目元数据记录（标题、分类等）；</li>
     *   <li>将原始文件上传至 MinIO 对象存储；</li>
     *   <li>通过 Apache Tika 提取纯文本；</li>
     *   <li>经 LangChain4j 分块、向量化后写入 Elasticsearch。</li>
     * </ol>
     *
     * @param file     管理员上传的原始文档
     * @param title    知识条目标题（必填）
     * @param category 知识分类（选填，如：美国/日本/通用）
     * @param keywords 搜索关键词，逗号分隔（选填）
     * @return 包含 source_file 路径的成功响应；摄取失败时返回错误信息
     */
    @PreAuthorize("@ss.hasPermi('visa:knowledge:add')")
    @Log(title = "签证知识库-文档上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload-doc")
    public AjaxResult uploadDoc(
            @RequestParam("file")                        MultipartFile file,
            @RequestParam("title")                       String        title,
            @RequestParam(value = "category",  required = false) String category,
            @RequestParam(value = "keywords",  required = false) String keywords)
    {
        if (file.isEmpty()) {
            return AjaxResult.error("请选择要上传的文档文件");
        }

        // 步骤 1：先在 MySQL 中创建知识条目元数据记录
        // content 置为空字符串以满足数据库 NOT NULL 约束；
        // 文件的实际文本内容由后续 Tika 提取后写入 Elasticsearch，不存入 MySQL content 列。
        VisaKnowledge knowledge = new VisaKnowledge();
        knowledge.setTitle(title);
        knowledge.setCategory(category);
        knowledge.setKeywords(keywords);
        knowledge.setContent("");
        knowledge.setStatus("0");
        visaKnowledgeService.insertVisaKnowledge(knowledge);

        try {
            // 步骤 2~5：MinIO 存储 → Tika 解析 → LangChain4j 分块向量化 → Elasticsearch
            String objectName = documentIngestionService.ingestDocument(file, knowledge);

            // 将 MinIO 对象路径回写到数据库，供管理员下载原文核对
            VisaKnowledge update = new VisaKnowledge();
            update.setId(knowledge.getId());
            update.setSourceFile(objectName);
            visaKnowledgeService.updateVisaKnowledge(update);

            return AjaxResult.success("文档摄取完成，MinIO 路径：" + objectName);
        } catch (Exception e) {
            return AjaxResult.error("文档摄取失败：" + e.getMessage());
        }
    }
}