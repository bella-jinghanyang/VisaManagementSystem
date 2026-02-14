package com.ruoyi.visa.service.impl;

import java.util.List;

import com.ruoyi.visa.mapper.VisaProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaTypeMapper;
import com.ruoyi.visa.domain.VisaType;
import com.ruoyi.visa.service.IVisaTypeService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 签证类型Service业务层处理
 * 
 * @author bella
 * @date 2026-01-23
 */
@Service
public class VisaTypeServiceImpl implements IVisaTypeService 
{
    @Autowired
    private VisaTypeMapper visaTypeMapper;

    @Autowired
    private VisaProductMapper visaProductMapper;

    /**
     * 查询签证类型
     * 
     * @param id 签证类型主键
     * @return 签证类型
     */
    @Override
    public VisaType selectVisaTypeById(Long id)
    {
        return visaTypeMapper.selectVisaTypeById(id);
    }

    /**
     * 查询签证类型列表
     * 
     * @param visaType 签证类型
     * @return 签证类型
     */
    @Override
    public List<VisaType> selectVisaTypeList(VisaType visaType)
    {
        return visaTypeMapper.selectVisaTypeList(visaType);
    }

    /**
     * 新增签证类型
     * 
     * @param visaType 签证类型
     * @return 结果
     */
    @Override
    public int insertVisaType(VisaType visaType)
    {
        return visaTypeMapper.insertVisaType(visaType);
    }

    /**
     * 修改签证类型
     * 
     * @param visaType 签证类型
     * @return 结果
     */
    @Override
    public int updateVisaType(VisaType visaType)
    {
        return visaTypeMapper.updateVisaType(visaType);
    }

    /**
     * 批量删除签证类型
     * 
     * @param ids 需要删除的签证类型主键
     * @return 结果
     */
    @Override
    public int deleteVisaTypeByIds(Long[] ids)
    {
        return visaTypeMapper.deleteVisaTypeByIds(ids);
    }

    /**
     * 删除签证类型信息
     * 
     * @param id 签证类型主键
     * @return 结果
     */
    @Override
    public int deleteVisaTypeById(Long id)
    {
        return visaTypeMapper.deleteVisaTypeById(id);
    }

    /**
     * 修改签证类型状态
     *
     * @param id 签证类型主键
     * @param status 状态
     * @return 结果
     */
    @Override
    @Transactional
    public int updateTypeStatus(Long id, String status) {
        // 1. 先更新签证类型表
        int rows = visaTypeMapper.updateTypeStatus(id, status);

        // 2. 级联逻辑：如果签证类型被“下架/停用”了 (假设 "0" 代表下架)
        if ("0".equals(status)) {
            // 把该签证类型下的所有产品状态改为 0 (下架)
            visaProductMapper.updateProductStatusByTypeId(id, 0);
        }

        return rows;
    }
}
