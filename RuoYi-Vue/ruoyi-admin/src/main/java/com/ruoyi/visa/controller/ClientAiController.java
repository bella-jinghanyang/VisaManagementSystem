package com.ruoyi.visa.controller;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.VisaAiService;
import com.ruoyi.visa.domain.VisaKnowledge;
import com.ruoyi.visa.service.IVisaKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/client/ai")
public class ClientAiController extends BaseController {

    @Autowired
    private VisaAiService aiService;

    @Autowired
    private IVisaKnowledgeService knowledgeService;

    @Anonymous
    @GetMapping("/chat")
    public AjaxResult chat(@RequestParam String q) {
        // 1. 【检索阶段】根据用户提问，去数据库匹配相关知识
        VisaKnowledge query = new VisaKnowledge();
        query.setStatus("0");
        List<VisaKnowledge> allKnowledge = knowledgeService.selectVisaKnowledgeList(query);

        StringBuilder context = new StringBuilder();
        for (VisaKnowledge k : allKnowledge) {
            // 逻辑：如果用户的问题里包含了知识库的关键词
            String[] kws = k.getKeywords().split(",");
            for (String kw : kws) {
                if (q.contains(kw.trim())) {
                    context.append(k.getContent()).append("\n");
                    break; // 匹配到一个关键词就拿走这段内容
                }
            }
        }

        // 2. 【增强阶段】如果没有搜到相关知识，给个兜底
        String finalContext = context.length() > 0 ? context.toString() : "暂无特定签证政策参考。";

        // 3. 【生成阶段】调 AI
        String result = aiService.getAiResponse(q, finalContext);

        return AjaxResult.success("查询成功", result);
    }
}