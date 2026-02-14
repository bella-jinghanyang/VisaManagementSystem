package com.ruoyi.visa.service.impl;

import java.util.List;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.visa.domain.VisaCountry;
import com.ruoyi.visa.domain.VisaDistrict;
import com.ruoyi.visa.domain.VisaType;
import com.ruoyi.visa.mapper.VisaCountryMapper;
import com.ruoyi.visa.mapper.VisaDistrictMapper;
import com.ruoyi.visa.mapper.VisaTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaProductMapper;
import com.ruoyi.visa.domain.VisaProduct;
import com.ruoyi.visa.service.IVisaProductService;

/**
 * 签证产品Service业务层处理
 * 
 * @author bella
 * @date 2026-01-24
 */
@Service
public class VisaProductServiceImpl implements IVisaProductService 
{
    @Autowired
    private VisaProductMapper visaProductMapper;
    @Autowired
    private VisaCountryMapper countryMapper;
    @Autowired
    private VisaDistrictMapper districtMapper;
    @Autowired
    private VisaTypeMapper typeMapper;

    /**
     * 查询签证产品
     * 
     * @param id 签证产品主键
     * @return 签证产品
     */
    @Override
    public VisaProduct selectVisaProductById(Long id)
    {
        return visaProductMapper.selectVisaProductById(id);
    }

    /**
     * 查询签证产品列表
     * 
     * @param visaProduct 签证产品
     * @return 签证产品
     */
    @Override
    public List<VisaProduct> selectVisaProductList(VisaProduct visaProduct)
    {
        return visaProductMapper.selectVisaProductList(visaProduct);
    }

    /**
     * 新增签证产品
     * 
     * @param visaProduct 签证产品
     * @return 结果
     */
    @Override
    public int insertVisaProduct(VisaProduct visaProduct)
    {
        visaProduct.setCreateTime(DateUtils.getNowDate());
        return visaProductMapper.insertVisaProduct(visaProduct);
    }

    /**
     * 修改签证产品
     * 
     * @param visaProduct 签证产品
     * @return 结果
     */
    @Override
    public int updateVisaProduct(VisaProduct visaProduct)
    {
        return visaProductMapper.updateVisaProduct(visaProduct);
    }

    /**
     * 批量删除签证产品
     * 
     * @param ids 需要删除的签证产品主键
     * @return 结果
     */
    @Override
    public int deleteVisaProductByIds(Long[] ids)
    {
        return visaProductMapper.deleteVisaProductByIds(ids);
    }

    /**
     * 删除签证产品信息
     * 
     * @param id 签证产品主键
     * @return 结果
     */
    @Override
    public int deleteVisaProductById(Long id)
    {
        return visaProductMapper.deleteVisaProductById(id);
    }

//    /**
//     * 修改产品状态
//     * @param id
//     * @param status
//     * @return
//     */
//    @Override
//    public int updateProductStatus(Long id, Integer status) {
//        return visaProductMapper.updateProductStatus(id, status);
//    }

    @Override
    public int changeProductStatus(VisaProduct visaProduct)
    {
        // ★★★ 核心逻辑开始 ★★★

        // 如果操作是“上架” (假设 1 代表上架)
        if (Integer.valueOf(1).equals(visaProduct.getStatus())) {

            // 情况A：如果是点击列表开关进来的，visaProduct 对象里可能只有 id 和 status，没有 countryId
            // 所以我们需要先去数据库查出这个产品的完整信息
            VisaProduct dbProduct = visaProductMapper.selectVisaProductById(visaProduct.getId());

            // 情况B：如果是编辑页面进来的，visaProduct 里可能有新的 countryId，优先用传进来的
            Long countryId = visaProduct.getCountryId() != null ? visaProduct.getCountryId() : dbProduct.getCountryId();
            Long districtId = visaProduct.getDistrictId() != null ? visaProduct.getDistrictId() : dbProduct.getDistrictId();
            Long typeId = visaProduct.getTypeId() != null ? visaProduct.getTypeId() : dbProduct.getTypeId();

            // 执行校验，如果校验不通过，这里直接抛异常，下面的 update 代码不会执行
            checkParentStatus(countryId, districtId, typeId);
        }

        // ★★★ 核心逻辑结束 ★★★

        visaProduct.setUpdateTime(DateUtils.getNowDate());
        return visaProductMapper.updateVisaProduct(visaProduct);
    }

    /**
     * 检查产品的关联配置是否可用
     * @param countryId 国家ID
     * @param districtId 领区ID
     * @param typeId 类型ID
     */
    private void checkParentStatus(Long countryId, Long districtId, Long typeId) {
        // 1. 检查国家
        VisaCountry country = countryMapper.selectVisaCountryById(countryId);
        // 注意：假设你的国家表 status 是 CHAR(1)，"0"代表下架
        if (country == null || "0".equals(country.getStatus())) {
            throw new ServiceException("所属国家【" + (country==null?"未知":country.getName()) + "】已下架，无法上架该产品！");
        }

        // 2. 检查领区
        VisaDistrict district = districtMapper.selectVisaDistrictById(districtId);
        if (district == null || "0".equals(district.getStatus())) {
            throw new ServiceException("所属领区【" + (district==null?"未知":district.getName()) + "】已停用，无法上架该产品！");
        }

        // 3. 检查类型
        VisaType type = typeMapper.selectVisaTypeById(typeId);
        if (type == null || "0".equals(type.getStatus())) {
            throw new ServiceException("所属签证类型【" + (type==null?"未知":type.getName()) + "】已停用，无法上架该产品！");
        }
    }
}
