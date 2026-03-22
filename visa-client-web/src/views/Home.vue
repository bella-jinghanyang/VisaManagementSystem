<template>
  <div class="home-wrapper">
    <!-- 1. 电影感首屏 (Hero Section) -->
    <section class="hero-section" :style="{ backgroundImage: 'url(' + heroImageUrl + ')' }">
      <div class="hero-mask"></div> <!-- 产生深邃感的渐变层 -->
      <div class="hero-content">
        <h1 class="hero-title">世界之门，<br/>智慧启程。</h1>
        <p class="hero-subtitle">基于 RAG 智慧引擎，为您重塑跨境签证办理体验。</p>

        <!-- 注册/登录主按钮 -->
        <div class="hero-action" v-if="!isLogin">
          <el-button type="primary" class="main-register-btn" @click="$router.push('/login')">
            立即开启您的旅程
          </el-button>
        </div>
      </div>

      <!-- 底部合作伙伴/支持机构 (模仿图中的品牌列) -->
      <div class="support-logos">
        <div class="logo-item"><i class="el-icon-location"></i> 全球直联</div>
        <div class="logo-item"><i class="el-icon-bank-card"></i> 安全支付</div>
        <div class="logo-item"><i class="el-icon-cpu"></i> AI 预审</div>
        <div class="logo-item"><i class="el-icon-truck"></i> 极速配送</div>
      </div>
    </section>

    <!-- 2. 签证办理流程 (6个阶段) -->
    <section class="process-container container-width">
      <div class="section-header-center">
        <h3>全流程闭环服务</h3>
        <p>从下单到配送，每一步都清晰可见</p>
      </div>
      <div class="workflow-steps">
        <div v-for="(step, index) in flowSteps" :key="index" class="step-item">
          <div class="step-icon-wrapper">
            <div class="step-icon-circle">
              <i :class="step.icon"></i>
            </div>
            <div v-if="index < flowSteps.length - 1" class="step-line"></div>
          </div>
          <h4>{{ step.title }}</h4>
          <p>{{ step.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 3. 智慧推荐 (已有的热门目的地) -->
    <section class="recommend-section container-width">
       <!-- ... 保持你之前的热门卡片代码 ... -->
    </section>
  </div>
</template>

<script>
/* eslint-disable */
import { listProduct } from '@/api/product';
import AppleCard from '@/components/AppleCard';

export default {
 components: { AppleCard },
  data() {
    return {
      heroImageUrl: require('@/assets/images/main1.jpg'), // 你自己找的图
      isLogin: localStorage.getItem('Client-Token') !== null,
      hotProducts: [],
      flowSteps: [
        { title: '提交订单', desc: '在线选择并下单', icon: 'el-icon-shopping-cart-full' },
        { title: '递交材料', desc: '上传办签电子件', icon: 'el-icon-upload' },
        { title: '审核材料', desc: '专员/AI双重核验', icon: 'el-icon-document-checked' },
        { title: '送签', desc: '护照送往大使馆', icon: 'el-icon-office-building' },
        { title: '出签', desc: '使馆审核结果反馈', icon: 'el-icon-circle-check' },
        { title: '配送', desc: '顺丰寄回原件', icon: 'el-icon-truck' }
      ]
    }
  },
  methods: {
    getHotProducts () {
      // 调接口获取前4个产品作为推荐
      listProduct({ pageNum: 1, pageSize: 4 }).then(res => {
        this.hotProducts = res.rows || []
      })
    },
    goDetail (id) {
      this.$router.push(`/product/${id}`)
    }
  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

/* 1. Hero 区域：深邃电影感 */
.hero-section {
  position: relative;
  height: 90vh;
  background-size: cover;
  background-position: center;
  background-attachment: fixed; /* 增加视差滚动感 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  overflow: hidden;

  .hero-mask {
    position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
    
      /* ★ 修改后的渐变逻辑 ★ */
      background: linear-gradient(180deg,
          rgba(0, 0, 0, 0.3) 0%,
          /* 顶部稍微暗一点，保住Navbar文字 */
          rgba(0, 0, 0, 0.15) 40%,
          /* 中间文字区域稍微压暗 */
          rgba(255, 255, 255, 0) 70%,
          /* 慢慢变亮 */
          #F7F9FC 100%
          /* 底部与网页背景完全融合 */
        );
  }

  .hero-content {
    position: relative;
    z-index: 2;
    text-align: center;
    .hero-title {
        font-size: 82px;
        font-weight: 800;
        color: #ffffff;
    
        /* ★ 核心修改：添加高级弥散投影 ★ */
        /* 参数说明：水平位移0，垂直位移4px，模糊半径30px，颜色为深蓝灰色且只有30%透明度 */
        text-shadow: 0 4px 30px rgba(44, 62, 80, 0.4),
          0 0 10px rgba(0, 0, 0, 0.1);
    
        letter-spacing: -1px; // 缩紧字间距，更显高级
    }
    .hero-subtitle {
       font-size: 22px;
        color: #ffffff;
        /* 副标题也加一层更淡的投影 */
        text-shadow: 0 2px 15px rgba(0, 0, 0, 0.3);
    }
    .main-register-btn {
      padding: 18px 45px;
      font-size: 18px;
      background: #fff !important;
      color: $primary-color !important;
      border-radius: 50px;
      font-weight: 700;
      box-shadow: 0 10px 30px rgba(0,0,0,0.1);
      &:hover { transform: scale(1.05); }
    }
  }
}

/* 品牌 Logo 列 */
.support-logos {
  position: absolute;
  bottom: 80px;
  display: flex;
  gap: 60px;
  z-index: 2;
  opacity: 0.8;
  .logo-item {
    font-size: 15px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    i { font-size: 20px; color: #fff; }
  }
}

/* 2. 办理流程样式 */
.process-container {
  padding: 100px 0;
  .section-header-center {
    text-align: center;
    margin-bottom: 60px;
    h3 { font-size: 32px; font-weight: 800; color: $text-main; }
    p { color: $text-secondary; margin-top: 10px; }
  }
}

.workflow-steps {
  display: flex;
  justify-content: space-between;
  .step-item {
    flex: 1;
    text-align: center;
    position: relative;
    
    .step-icon-wrapper {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      .step-icon-circle {
        width: 64px; height: 64px;
        background: #fff;
        border-radius: 22px; // Apple 连续曲率圆角
        display: flex; align-items: center; justify-content: center;
        font-size: 24px; color: $primary-color;
        box-shadow: $shadow-base;
        z-index: 2;
      }
      .step-line {
        flex: 1; height: 2px; background: #EBF5FF; margin: 0 -10px;
      }
    }
    h4 { font-size: 16px; font-weight: 700; margin-bottom: 8px; color: $text-main; }
    p { font-size: 12px; color: #999; padding: 0 15px; }
  }
}
</style>