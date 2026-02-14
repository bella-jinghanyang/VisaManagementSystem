<template>
  <div class="detail-container container-width" v-if="product.id">
    <!-- 面包屑：回家的路 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/products' }">签证大厅</el-breadcrumb-item>
      <el-breadcrumb-item>详情</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="detail-layout">
      <!-- 左侧：产品详细介绍 -->
      <div class="main-info">
        <!-- 1. 标题头 -->
        <apple-card class="mb-30">
          <div class="product-header">
            <div class="title-row">
              <h1>{{ product.title }}</h1>
              <div class="status-tag">服务中</div>
            </div>
            <div class="tags-row">
              <span class="meta-tag"><i class="el-icon-location-outline"></i> {{ product.countryName }}</span>
              <span class="meta-tag"><i class="el-icon-office-building"></i> {{ product.districtName }}</span>
              <span class="meta-tag"><i class="el-icon-tickets"></i> {{ product.typeName }}</span>
              <span class="meta-tag" v-if="product.isInterviewRequired"><i class="el-icon-user"></i> 需面签</span>
            </div>
          </div>
        </apple-card>

        <!-- 2. 服务保障 -->
        <div class="guarantee-row mb-30">
          <div class="gua-item">
            <i class="el-icon-circle-check"></i>
            <span>官方直办</span>
          </div>
          <div class="gua-item">
            <i class="el-icon-coordinate"></i>
            <span>专家预审</span>
          </div>
          <div class="gua-item">
            <i class="el-icon-refresh-left"></i>
            <span>拒签退款</span>
          </div>
          <div class="gua-item">
            <i class="el-icon-lock"></i>
            <span>隐私保护</span>
          </div>
        </div>

        <!-- 3. 所需材料 (最重要) -->
        <apple-card class="mb-30">
          <h3 class="section-title">准备材料</h3>
          <p class="section-desc">请提前准备好以下电子版或原件材料</p>

          <div class="material-list">
            <div v-for="(item, index) in materials" :key="index" class="material-row">
              <div class="item-icon">
                <i :class="getIcon(item.type)"></i>
              </div>
              <div class="item-content">
                <div class="item-name">
                  {{ item.name }}
                  <span class="must" v-if="item.required">必选</span>
                </div>
                <div class="item-desc">{{ item.desc || '请根据要求上传清晰扫描件' }}</div>
              </div>
             <div class="item-action">
                <el-button type="text" :icon="item.templateUrl ? 'el-icon-download' : 'el-icon-info'"
                  :class="{ 'is-disabled': !item.templateUrl }" :disabled="!item.templateUrl"
                  @click="item.templateUrl ? downloadTemplate(item.templateUrl) : null">
                  {{ item.templateUrl ? '下载模板' : '无需模板' }}
                </el-button>
              </div>
            </div>
          </div>
        </apple-card>
      </div>

      <!-- 右侧：悬浮下单栏 -->
      <div class="side-bar">
        <apple-card class="sticky-card">
          <div class="price-box">
            <span class="unit">￥</span>
            <span class="amount">{{ product.price }}</span>
          </div>

          <div class="specs-list">
            <div class="spec-item">
              <span class="label">办理时长</span>
              <span class="val">{{ product.processingTime || '5-7个工作日' }}</span>
            </div>
            <div class="spec-item">
              <span class="label">有效期</span>
              <span class="val">{{ product.validityPeriod || '根据使馆批复' }}</span>
            </div>
            <div class="spec-item">
              <span class="label">停留天数</span>
              <span class="val">{{ product.maxStayDays || '15-30天' }}</span>
            </div>
          </div>

          <div class="action-buttons">
            <!-- 立即办理：主动作 -->
            <el-button type="primary" class="btn-main" @click="handleBuyNow">立即办理</el-button>

            <!-- 加入购物车：次动作 -->
            <el-button class="btn-sub" icon="el-icon-shopping-cart-2" @click="handleAddToCart">
              加入购物车
            </el-button>
          </div>

          <p class="bottom-tips">支付成功后将开启材料收集流程</p>
        </apple-card>
      </div>
    </div>
  </div>
</template>

<script>
import AppleCard from '@/components/AppleCard'
import { getProduct } from '@/api/product'
import { addToCart } from '@/api/cart'

export default {
  name: 'ProductDetail',
  components: { AppleCard },
  data () {
    return {
      product: {},
      materials: []
    }
  },
  created () {
    this.initData()
  },
  methods: {
    initData () {
      const id = this.$route.params.id
      getProduct(id).then(res => {
        this.product = res.data
        // 解析 JSON 材料配置
        if (res.data.requirementsConfig) {
          this.materials = JSON.parse(res.data.requirementsConfig)
        }
      })
    },
    getIcon (type) {
      const maps = {
        image: 'el-icon-picture-outline',
        file: 'el-icon-document',
        text: 'el-icon-edit-outline'
      }
      return maps[type] || 'el-icon-document'
    },
    downloadTemplate (url) {
      window.open(this.$getImgUrl(url), '_blank')
    },
    handleAddToCart () {
    // 1. 权限拦截
      const userStr = localStorage.getItem('Client-User')
      if (!userStr) {
        this.$message.warning('请先登录再添加')
        this.$router.push('/login')
        return
      }
      const user = JSON.parse(userStr)

      // 2. 发起请求
      const data = {
        customerId: user.id,
        productId: this.product.id
      }

      addToCart(data).then(res => {
        this.$message({
          message: '成功加入预选清单',
          type: 'success'
        })
        // ★★★ 关键：通知 Navbar 更新角标 ★★★
        this.$bus.$emit('cart-updated')
      })
    },
    handleBuyNow () {
      // 检查登录
      if (!localStorage.getItem('Client-Token')) {
        this.$message.warning('请先登录再办理')
        this.$router.push('/login')
        return
      }
      this.$router.push({
        path: '/order/create',
        query: { productId: this.product.id }
      })
    }
  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

.detail-container { padding: 40px 0 100px; }
.breadcrumb { margin-bottom: 30px; }

.detail-layout {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.main-info { flex: 1; }
.side-bar { width: 380px; }

.mb-30 { margin-bottom: 30px; }

/* 标题样式 */
.product-header {
  .title-row {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;
    h1 { font-size: 32px; font-weight: 700; color: $text-main; }
    .status-tag {
      background: rgba(106, 175, 230, 0.1);
      color: $primary-color;
      padding: 4px 12px;
      border-radius: 50px;
      font-size: 14px;
      font-weight: 600;
    }
  }
  .tags-row {
    display: flex;
    gap: 20px;
    .meta-tag { color: $text-secondary; font-size: 15px; i { margin-right: 4px; } }
  }
}

/* 服务保障 */
.guarantee-row {
  display: flex;
  justify-content: space-around;
  background: rgba(255,255,255,0.6);
  padding: 20px;
  border-radius: $radius-card;
  .gua-item {
    display: flex;
    align-items: center;
    gap: 8px;
    color: $text-main;
    font-size: 14px;
    font-weight: 500;
    i { color: $primary-color; font-size: 18px; }
  }
}

/* 材料列表 */
.section-title { font-size: 22px; font-weight: 700; margin-bottom: 8px; }
.section-desc { color: $text-secondary; font-size: 14px; margin-bottom: 30px; }

.material-row {
  display: flex;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #F0F2F5;
  &:last-child { border-bottom: none; }

  .item-icon {
    width: 48px; height: 48px;
    background: #F7F9FC;
    border-radius: 14px;
    display: flex; align-items: center; justify-content: center;
    font-size: 22px; color: $primary-color;
    margin-right: 20px;
  }
  .item-content {
    flex: 1;
    .item-name {
      font-size: 17px; font-weight: 600; margin-bottom: 4px;
      .must { font-size: 11px; background: #FF6B6B; color: #fff; padding: 2px 6px; border-radius: 4px; margin-left: 8px; vertical-align: middle; }
    }
    .item-desc { color: $text-secondary; font-size: 14px; }
  }

  /* 操作区：固定宽度，确保左侧对齐 */
  .item-action {
    width: 100px; /* ★ 给一个固定的宽度，就像一堵墙一样挡在那里 */
    text-align: right;
    margin-left: 20px;

    .el-button {
      font-weight: 600;
      font-size: 13px;
      transition: all 0.3s;

      /* 有链接时的颜色 (奶蓝色) */
      color: $primary-color;

      /* 重点：当被禁用时的样式 (灰色) */
      &.is-disabled {
        color: #C0C4CC !important; /* 浅灰色 */
        cursor: not-allowed;       /* 鼠标变成禁止图标 */
        background: transparent;

        i { color: #DCDFE6; }      /* 图标更淡一点 */
      }
    }
  }
}

/* 右侧侧边栏 */
.sticky-card {
  position: sticky; top: 110px;
  .price-box {
    margin-bottom: 30px;
    .unit { font-size: 20px; color: $primary-color; font-weight: 700; }
    .amount { font-size: 48px; color: $primary-color; font-weight: 800; }
  }
  .specs-list {
    background: #F7F9FC;
    border-radius: $radius-base;
    padding: 20px;
    margin-bottom: 30px;
    .spec-item {
      display: flex; justify-content: space-between; margin-bottom: 12px;
      &:last-child { margin-bottom: 0; }
      .label { color: $text-secondary; font-size: 14px; }
      .val { color: $text-main; font-size: 14px; font-weight: 600; }
    }
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
  .btn-main { height: 56px; font-size: 18px; border-radius: 16px !important; }
  .btn-sub {
    height: 56px; font-size: 16px; border-radius: 16px !important;
    border: 2px solid $primary-color !important;
    color: $primary-color;
    background: transparent;
    &:hover { background: rgba(106, 175, 230, 0.05); }
  }
}

.bottom-tips { text-align: center; color: #C0C4CC; font-size: 12px; margin-top: 20px; }
</style>
