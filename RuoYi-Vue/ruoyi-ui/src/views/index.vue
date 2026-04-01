<template>
    <div class="dashboard-page">
        <div class="dashboard-inner" v-loading="loading">

            <!-- 1. 欢迎 Header -->
            <div class="welcome-header">
                <h1>控制台概览</h1>
                <p>今天是 {{ currentDate }}，这是您今日的业务看板。</p>
            </div>

            <!-- 2. 顶部核心指标 -->
            <el-row :gutter="30" class="section-gap flex-center">
                <el-col :span="6" v-for="(stat, index) in topStats" :key="index">
                    <div class="stat-card apple-card">
                        <div class="icon-box" :style="{ background: stat.bg }">
                            <i :class="stat.icon" :style="{ color: stat.color }"></i>
                        </div>
                        <div class="content">
                            <div class="label">{{ stat.title }}</div>
                            <div class="value">{{ stat.value }}</div>
                        </div>
                    </div>
                </el-col>
            </el-row>

            <!-- 3. 图表区域 -->
            <el-row :gutter="40" class="section-gap flex-center">
                <el-col :span="14">
                    <div class="chart-card apple-card">
                        <div class="card-title">办理转化漏斗</div>
                        <div class="chart-body" id="funnelChart"></div>
                    </div>
                </el-col>
                <el-col :span="14">
                    <div class="chart-card apple-card">
                        <div class="card-title">热门目的地分布</div>
                        <div class="chart-body" id="pieChart"></div>
                    </div>
                </el-col>
            </el-row>

            <!-- 4. 滞留预警列表 -->
            <div class="warning-section section-gap">
                <div class="apple-card table-card">
                    <div class="card-header-flex">
                        <div class="card-title danger-text">
                            <i class="el-icon-warning"></i> 异常滞留预警 (超7天未变动)
                        </div>
                        <el-tag type="danger" effect="dark" size="small" round>重点关注单据</el-tag>
                    </div>
                    <el-table :data="warningOrders" style="width: 100%" size="large">
                        <el-table-column prop="orderNo" label="订单号" width="220" />
                        <el-table-column label="产品名称" prop="productName" show-overflow-tooltip />
                        <el-table-column label="当前阶段" width="180" align="center">
                            <template slot-scope="scope">
                                <el-tag type="info" plain size="small">{{ scope.row.statusName }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="滞留时长" width="150" align="center">
                            <template slot-scope="scope">
                                <span class="delay-day">{{ scope.row.delayDays }} 天</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="120" align="center">
                            <template slot-scope="scope">
                                <el-button type="primary" size="mini" round @click="goOrderList">去处理</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>

        </div>
    </div>
</template>

<script>
import * as echarts from 'echarts';
import request from '@/utils/request';

export default {
    data() {
        return {
            loading: true,
            currentDate: new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }),
            topStats: [
                { title: '待初审材料', value: '0', icon: 'el-icon-document', color: '#007AFF', bg: 'rgba(0,122,255,0.08)' },
                { title: '待签收原件', value: '0', icon: 'el-icon-box', color: '#FF9500', bg: 'rgba(255,149,0,0.08)' },
                { title: '本月总营收', value: '￥0', icon: 'el-icon-money', color: '#34C759', bg: 'rgba(52,199,89,0.08)' },
                { title: '服务平均分', value: '0.0', icon: 'el-icon-star-off', color: '#AF52DE', bg: 'rgba(175,82,222,0.08)' }
            ],
            warningOrders: []
        }
    },
    mounted() {
        this.initDashboard();
    },
    methods: {
        initDashboard() {
            this.loading = true;
            // ★ 核心：请求后端统计接口
            request({ url: '/visa/index/data', method: 'get' }).then(res => {
                const data = res.data;
                // 1. 填充顶部
                this.topStats[0].value = data.pendingReview;
                this.topStats[1].value = data.pendingPhysical;
                this.topStats[2].value = '￥' + (data.monthlyRevenue || 0).toLocaleString();
                this.topStats[3].value = (data.avgRating || 5.0).toFixed(1);

                // 2. 填充表格
                this.warningOrders = data.warningOrders;

                // 3. 渲染图表
                this.$nextTick(() => {
                    this.renderCharts(data.funnelData, data.pieData);
                });
                this.loading = false;
            });
        },

        renderCharts(funnelData, pieData) {
            // 漏斗图
            const funnel = echarts.init(document.getElementById('funnelChart'));
            funnel.setOption({
                color: ['#007AFF', '#5AC8FA', '#34C759', '#FFD60A'],
                series: [{
                    type: 'funnel',
                    left: '10%', width: '80%',
                    data: funnelData
                }]
            });

            // 饼图
            const pie = echarts.init(document.getElementById('pieChart'));
            pie.setOption({
                tooltip: { trigger: 'item' },
                legend: { bottom: '0', left: 'center' },
                series: [{
                    type: 'pie',
                    radius: ['45%', '70%'],
                    itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
                    data: pieData
                }]
            });

            window.onresize = () => { funnel.resize(); pie.resize(); };
        },
        goOrderList() { this.$router.push('/visa/order'); }
    }
}
</script>

<style scoped lang="scss">
.dashboard-page {
    background: #F5F5F7;
    min-height: 100vh;
    display: flex;
    justify-content: center;
    /* 解决靠左问题 */
}

.dashboard-inner {
    width: 95%;
    max-width: 1400px;
    padding: 40px 0;
}

.welcome-header {
    margin-bottom: 50px;

    h1 {
        font-size: 34px;
        font-weight: 700;
        color: #1d1d1f;
    }
}

.section-gap {
    margin-bottom: 50px;
}

.flex-center {
    display: flex;
    align-items: stretch;
}

.apple-card {
    background: #fff;
    border-radius: 28px;
    padding: 30px;
    border: 1px solid rgba(0, 0, 0, 0.04);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.02);
    transition: all 0.3s;

    &:hover {
        transform: translateY(-5px);
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
    }
}

.stat-card {
    display: flex;
    align-items: center;

    .icon-box {
        width: 64px;
        height: 64px;
        border-radius: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20px;

        i {
            font-size: 28px;
        }
    }

    .value {
        font-size: 28px;
        font-weight: 700;
    }
}

.chart-card {
    height: 500px;
    display: flex;
    flex-direction: column;

    .chart-body {
        flex: 1;
    }
}

.card-title {
    font-size: 19px;
    font-weight: 700;
    margin-bottom: 20px;
}

.danger-text {
    color: #FF3B30;
}

.delay-day {
    color: #FF3B30;
    font-weight: 700;
}
</style>