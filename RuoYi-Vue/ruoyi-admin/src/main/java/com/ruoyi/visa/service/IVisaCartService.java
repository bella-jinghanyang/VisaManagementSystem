package com.ruoyi.visa.service;

import java.util.List;
import com.ruoyi.visa.domain.VisaCart;

/**
 * 购物车Service接口
 * 
 * @author bella
 * @date 2026-01-29
 */
public interface IVisaCartService 
{
    /**
     * 查询购物车
     * 
     * @param id 购物车主键
     * @return 购物车
     */
    public VisaCart selectVisaCartById(Long id);

    /**
     * 查询购物车列表
     * 
     * @param visaCart 购物车
     * @return 购物车集合
     */
    public List<VisaCart> selectVisaCartList(VisaCart visaCart);

    /**
     * 新增购物车
     * 
     * @param visaCart 购物车
     * @return 结果
     */
    public int insertVisaCart(VisaCart visaCart);

    /**
     * 修改购物车
     * 
     * @param visaCart 购物车
     * @return 结果
     */
    public int updateVisaCart(VisaCart visaCart);

    /**
     * 批量删除购物车
     * 
     * @param ids 需要删除的购物车主键集合
     * @return 结果
     */
    public int deleteVisaCartByIds(Long[] ids);

    /**
     * 删除购物车信息
     * 
     * @param id 购物车主键
     * @return 结果
     */
    public int deleteVisaCartById(Long id);
}
