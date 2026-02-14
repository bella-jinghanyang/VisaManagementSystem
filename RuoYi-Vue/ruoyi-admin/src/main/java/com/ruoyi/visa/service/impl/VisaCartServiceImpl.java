package com.ruoyi.visa.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.visa.mapper.VisaCartMapper;
import com.ruoyi.visa.domain.VisaCart;
import com.ruoyi.visa.service.IVisaCartService;

/**
 * 购物车Service业务层处理
 * 
 * @author bella
 * @date 2026-01-29
 */
@Service
public class VisaCartServiceImpl implements IVisaCartService 
{
    @Autowired
    private VisaCartMapper visaCartMapper;

    /**
     * 查询购物车
     * 
     * @param id 购物车主键
     * @return 购物车
     */
    @Override
    public VisaCart selectVisaCartById(Long id)
    {
        return visaCartMapper.selectVisaCartById(id);
    }

    /**
     * 查询购物车列表
     * 
     * @param visaCart 购物车
     * @return 购物车
     */
    @Override
    public List<VisaCart> selectVisaCartList(VisaCart visaCart)
    {
        return visaCartMapper.selectVisaCartList(visaCart);
    }

    /**
     * 新增购物车
     * 
     * @param visaCart 购物车
     * @return 结果
     */
    @Override
    public int insertVisaCart(VisaCart visaCart)
    {
        visaCart.setCreateTime(DateUtils.getNowDate());
        return visaCartMapper.insertVisaCart(visaCart);
    }

    /**
     * 修改购物车
     * 
     * @param visaCart 购物车
     * @return 结果
     */
    @Override
    public int updateVisaCart(VisaCart visaCart)
    {
        return visaCartMapper.updateVisaCart(visaCart);
    }

    /**
     * 批量删除购物车
     * 
     * @param ids 需要删除的购物车主键
     * @return 结果
     */
    @Override
    public int deleteVisaCartByIds(Long[] ids)
    {
        return visaCartMapper.deleteVisaCartByIds(ids);
    }

    /**
     * 删除购物车信息
     * 
     * @param id 购物车主键
     * @return 结果
     */
    @Override
    public int deleteVisaCartById(Long id)
    {
        return visaCartMapper.deleteVisaCartById(id);
    }
}
