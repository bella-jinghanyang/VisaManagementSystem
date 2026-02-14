package com.ruoyi.visa.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.CCustomerMapper;
import com.ruoyi.visa.domain.CCustomer;
import com.ruoyi.visa.service.ICCustomerService;

/**
 * C端客户Service业务层处理
 * 
 * @author bella
 * @date 2026-01-26
 */
@Service
public class CCustomerServiceImpl implements ICCustomerService 
{
    @Autowired
    private CCustomerMapper cCustomerMapper;

    /**
     * 查询C端客户
     * 
     * @param id C端客户主键
     * @return C端客户
     */
    @Override
    public CCustomer selectCCustomerById(Long id)
    {
        return cCustomerMapper.selectCCustomerById(id);
    }

    /**
     * 查询C端客户列表
     * 
     * @param cCustomer C端客户
     * @return C端客户
     */
    @Override
    public List<CCustomer> selectCCustomerList(CCustomer cCustomer)
    {
        return cCustomerMapper.selectCCustomerList(cCustomer);
    }

    /**
     * 新增C端客户
     * 
     * @param cCustomer C端客户
     * @return 结果
     */
    @Override
    public int insertCCustomer(CCustomer cCustomer)
    {
        cCustomer.setCreateTime(DateUtils.getNowDate());
        return cCustomerMapper.insertCCustomer(cCustomer);
    }

    /**
     * 修改C端客户
     * 
     * @param cCustomer C端客户
     * @return 结果
     */
    @Override
    public int updateCCustomer(CCustomer cCustomer)
    {
        cCustomer.setUpdateTime(DateUtils.getNowDate());
        return cCustomerMapper.updateCCustomer(cCustomer);
    }

    /**
     * 批量删除C端客户
     * 
     * @param ids 需要删除的C端客户主键
     * @return 结果
     */
    @Override
    public int deleteCCustomerByIds(Long[] ids)
    {
        return cCustomerMapper.deleteCCustomerByIds(ids);
    }

    /**
     * 删除C端客户信息
     * 
     * @param id C端客户主键
     * @return 结果
     */
    @Override
    public int deleteCCustomerById(Long id)
    {
        return cCustomerMapper.deleteCCustomerById(id);
    }

    /**
     * 修改客户状态
     * @param id
     * @param status
     * @return
     */
    @Override
    public int updateCustomerStatus(Long id, String status) {
        return cCustomerMapper.updateCustomerStatus(id, status);
    }
}
