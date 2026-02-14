package com.ruoyi.visa.service;

import java.util.List;
import com.ruoyi.visa.domain.VisaProduct;

/**
 * 签证产品Service接口
 * 
 * @author bella
 * @date 2026-01-24
 */
public interface IVisaProductService 
{
    /**
     * 查询签证产品
     * 
     * @param id 签证产品主键
     * @return 签证产品
     */
    public VisaProduct selectVisaProductById(Long id);

    /**
     * 查询签证产品列表
     * 
     * @param visaProduct 签证产品
     * @return 签证产品集合
     */
    public List<VisaProduct> selectVisaProductList(VisaProduct visaProduct);

    /**
     * 新增签证产品
     * 
     * @param visaProduct 签证产品
     * @return 结果
     */
    public int insertVisaProduct(VisaProduct visaProduct);

    /**
     * 修改签证产品
     * 
     * @param visaProduct 签证产品
     * @return 结果
     */
    public int updateVisaProduct(VisaProduct visaProduct);

    /**
     * 批量删除签证产品
     * 
     * @param ids 需要删除的签证产品主键集合
     * @return 结果
     */
    public int deleteVisaProductByIds(Long[] ids);

    /**
     * 删除签证产品信息
     * 
     * @param id 签证产品主键
     * @return 结果
     */
    public int deleteVisaProductById(Long id);

//    /** 修改产品状态
//     *
//     * @param id
//     * @param status
//     * @return
//     */
//    int updateProductStatus(Long id, Integer status);


    int changeProductStatus(VisaProduct visaProduct);
}
