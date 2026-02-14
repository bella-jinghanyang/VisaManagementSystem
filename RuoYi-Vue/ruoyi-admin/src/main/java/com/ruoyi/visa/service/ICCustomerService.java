package com.ruoyi.visa.service;

import java.util.List;
import com.ruoyi.visa.domain.CCustomer;

/**
 * C端客户Service接口
 * 
 * @author bella
 * @date 2026-01-26
 */
public interface ICCustomerService 
{
    /**
     * 查询C端客户
     * 
     * @param id C端客户主键
     * @return C端客户
     */
    public CCustomer selectCCustomerById(Long id);

    /**
     * 查询C端客户列表
     * 
     * @param cCustomer C端客户
     * @return C端客户集合
     */
    public List<CCustomer> selectCCustomerList(CCustomer cCustomer);

    /**
     * 新增C端客户
     * 
     * @param cCustomer C端客户
     * @return 结果
     */
    public int insertCCustomer(CCustomer cCustomer);

    /**
     * 修改C端客户
     * 
     * @param cCustomer C端客户
     * @return 结果
     */
    public int updateCCustomer(CCustomer cCustomer);

    /**
     * 批量删除C端客户
     * 
     * @param ids 需要删除的C端客户主键集合
     * @return 结果
     */
    public int deleteCCustomerByIds(Long[] ids);

    /**
     * 删除C端客户信息
     * 
     * @param id C端客户主键
     * @return 结果
     */
    public int deleteCCustomerById(Long id);

    /**
     * 修改客户状态
     * @param id
     * @param status
     * @return
     */
    int updateCustomerStatus(Long id, String status);
}
