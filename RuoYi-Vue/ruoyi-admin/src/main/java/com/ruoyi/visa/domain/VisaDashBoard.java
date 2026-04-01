package com.ruoyi.visa.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class VisaDashBoard {
    // 顶部四个卡片
    private Long pendingReview;    // 待初审
    private Long pendingPhysical;  // 待签收原件
    private BigDecimal monthlyRevenue; // 本月营收

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public Long getPendingReview() {
        return pendingReview;
    }

    public void setPendingReview(Long pendingReview) {
        this.pendingReview = pendingReview;
    }

    public Long getPendingPhysical() {
        return pendingPhysical;
    }

    public void setPendingPhysical(Long pendingPhysical) {
        this.pendingPhysical = pendingPhysical;
    }

    public BigDecimal getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(BigDecimal monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public List<Map<String, Object>> getFunnelData() {
        return funnelData;
    }

    public void setFunnelData(List<Map<String, Object>> funnelData) {
        this.funnelData = funnelData;
    }

    public List<Map<String, Object>> getPieData() {
        return pieData;
    }

    public void setPieData(List<Map<String, Object>> pieData) {
        this.pieData = pieData;
    }

    public List<Map<String, Object>> getWarningOrders() {
        return warningOrders;
    }

    public void setWarningOrders(List<Map<String, Object>> warningOrders) {
        this.warningOrders = warningOrders;
    }

    private Double avgRating;      // 平均评分

    // 漏斗图数据
    private List<Map<String, Object>> funnelData;

    // 饼图数据
    private List<Map<String, Object>> pieData;

    // 预警列表
    private List<Map<String, Object>> warningOrders;


}