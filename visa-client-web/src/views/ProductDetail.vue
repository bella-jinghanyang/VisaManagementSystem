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

        <!-- 在 ProductDetail.vue 的 main-info 区域插入 -->
        <apple-card class="mb-30 range-card">
          <h3 class="section-title">办理范围</h3>
          <div class="range-content">
            <div class="district-info">
              <span class="current-d">{{ product.districtName }}</span>
              <span class="area-list">{{ product.coverArea }}</span>
            </div>
            <div class="rule-box">
              <p><i class="el-icon-info"></i> <strong>户籍要求：</strong> 护照签发地或常住地需在上述省份内。</p>
              <p><i class="el-icon-postcard"></i> <strong>异地办理：</strong> 若户籍不在以上省份，需提供连续 6 个月的社保缴纳证明或有效的居住证。</p>
            </div>
          </div>
        </apple-card>

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

        <!-- 4. 评分与评论 (重构版：内容靠左 + 真实分布条) -->
<apple-card class="mb-30 reviews-section">
  <h3 class="section-title">评分与评论</h3>

  <div class="reviews-container">
    <!-- 评分概览区 -->
    <div class="reviews-header">
      <div class="summary-box">
        <div class="score-num">{{ calculateStats.average }}</div>
        <div class="score-right">
          <div class="stars-text">满分 5 分</div>
          <el-rate v-model="calculateStats.avgNum" disabled text-color="#ff9900"></el-rate>
          <div class="total-reviews">{{ reviews.length }} 条评分</div>
        </div>
      </div>

            <!-- 评分分布进度条 (模拟 App Store) -->
              <div class="rating-bars">
                <div v-for="star in [5, 4, 3, 2, 1]" :key="star" class="bar-item">
                  <span class="star-label">{{ star }}</span>
                  <div class="bar-bg">
                    <div class="bar-fill" :style="{ width: calculateStats.distribution[star] + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 评论列表 (确保靠左) -->
            <div class="reviews-list">
              <div v-for="rev in reviews" :key="rev.id" class="review-item">
                <div class="rev-user">
                  <el-avatar :size="32" :src="$getImgUrl(rev.customerAvatar)"></el-avatar>
                  <div class="user-meta">
                    <span class="username">{{ maskName(rev.customerNickname) }}</span>
                    <el-rate v-model="rev.rating" disabled class="mini-rate"></el-rate>
                  </div>
                  <span class="rev-date">{{ parseTime(rev.createTime, '{y}-{m}-{d}') }}</span>
                </div>

                <p class="rev-content">{{ rev.content }}</p>

                <!-- 晒图 -->
                <div class="rev-images" v-if="rev.images">
                  <el-image v-for="(img, i) in parseJson(rev.images)" :key="i" :src="$getImgUrl(img)"
                    :preview-src-list="[...parseJson(rev.images).map(url => $getImgUrl(url))]" class="rev-img-mini"
                    fit="cover" />
                </div>

                <!-- 官方回复 -->
                <div class="admin-reply" v-if="rev.adminReply">
                  <span class="reply-label">官方回复</span>
                  <p>{{ rev.adminReply }}</p>
                </div>
              </div>

              <!-- 无评价时的占位 -->
              <div v-if="reviews.length === 0" class="no-reviews">
                暂时没有评价，快去办理并分享您的心得吧。
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
import { getProductComments } from '@/api/comment'

export default {
  name: 'ProductDetail',
  components: { AppleCard },
  data () {
    return {
      product: {},
      materials: [],
      reviews: [] // ★ 评价列表

    }
  },
  created () {
    this.initData()
    this.getComments() // ★ 加载评价
  },
  computed: {
    /** ★★★ 核心逻辑：计算真实评分统计 ★★★ */
    calculateStats () {
      const len = this.reviews.length
      if (len === 0) {
        return { average: '0.0', avgNum: 0, distribution: { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 } }
      }

      // 1. 计算平均分
      const sum = this.reviews.reduce((acc, cur) => acc + cur.rating, 0)
      const avg = (sum / len).toFixed(1)

      // 2. 计算各星级分布比例
      const dist = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
      this.reviews.forEach(r => {
        if (dist[r.rating] !== undefined) dist[r.rating]++
      })

      // 转换为百分比
      Object.keys(dist).forEach(key => {
        dist[key] = (dist[key] / len) * 100
      })

      return {
        average: avg,
        avgNum: parseFloat(avg),
        distribution: dist
      }
    }
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
    },
    /** ★ 获取评价数据 ★ */
    getComments () {
      const productId = this.$route.params.id
      getProductComments(productId).then(res => {
        // 修正点：若依的查询接口如果是生成的，数据通常在 rows 里；如果是自定义 AjaxResult，在 data 里
        this.reviews = res.data || res.rows || []
        console.log('评价列表数据：', this.reviews)
      })
    },

    /** ★ 辅助：解析图片数组 ★ */
    parseJson (str) {
      if (!str) return []
      try {
        return typeof str === 'string' ? JSON.parse(str) : str
      } catch (e) { return [] }
    },

    /** ★ 辅助：名字打码 ★ */
    maskName (name) {
      if (!name) return '匿名用户'
      if (name.length <= 1) return name + '***'
      return name.charAt(0) + '***' + name.charAt(name.length - 1)
    },
    /** 时间格式化工具 */
    parseTime (time, pattern) {
      if (arguments.length === 0 || !time) {
        return null
      }
      const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
      let date
      if (typeof time === 'object') {
        date = time
      } else {
        if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
          time = parseInt(time)
        } else if (typeof time === 'string') {
          time = time.replace(/-/gm, '/')
        }
        if ((typeof time === 'number') && (time.toString().length === 10)) {
          time = time * 1000
        }
        date = new Date(time)
      }
      const formatObj = {
        y: date.getFullYear(),
        m: date.getMonth() + 1,
        d: date.getDate(),
        h: date.getHours(),
        i: date.getMinutes(),
        s: date.getSeconds(),
        a: date.getDay()
      }
      const timeStr = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
        let value = formatObj[key]
        if (result.length > 0 && value < 10) {
          value = '0' + value
        }
        return value || 0
      })
      return timeStr
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

.range-card {
  background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%) !important;
}

.district-info {
  margin-bottom: 15px;
  .current-d {
    font-size: 18px;
    font-weight: 700;
    color: $primary-color;
    margin-right: 15px;
  }
  .area-list {
    font-size: 14px;
    color: $text-main;
  }
}

.rule-box {
  background: rgba(255, 255, 255, 0.5);
  padding: 15px;
  border-radius: 12px;
  p {
    font-size: 13px;
    color: #666;
    margin-bottom: 8px;
    &:last-child { margin-bottom: 0; }
    i { color: $primary-color; margin-right: 5px; }
  }
}

/* 评价区主容器 */
.reviews-container {
  text-align: left !important; /* ★ 强制所有内容靠左对齐 ★ */
}

.reviews-header {
  display: flex;
  align-items: center;
  gap: 50px;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #f0f2f5;

  .summary-box {
    text-align: center; /* 唯独分数汇总可以居中，很有仪式感 */
    .score-num { font-size: 64px; font-weight: 800; color: $text-main; line-height: 1; margin-bottom: 10px; }
    .stars-text { font-size: 12px; color: #999; font-weight: 700; }
    .total-reviews { font-size: 12px; color: #ccc; margin-top: 5px; }
  }

  /* 分布进度条样式 */
  .rating-bars {
    flex: 1;
    max-width: 300px;
    .bar-item {
      display: flex; align-items: center; gap: 10px; margin-bottom: 4px;
      .star-label { font-size: 11px; color: #999; width: 10px; font-weight: 700; }
      .bar-bg { flex: 1; height: 6px; background: #E9E9EB; border-radius: 3px; overflow: hidden; }
      .bar-fill { height: 100%; background: #6AAFE6; border-radius: 3px; } /* 使用奶蓝色填充 */
    }
  }
}

/* 列表项靠左 */
.reviews-list {
  text-align: left !important;
}

.review-item {
  padding: 30px 0;
  border-bottom: 1px solid #f5f7fa;

  .rev-user {
    display: flex; align-items: center; gap: 12px;
    .user-meta {
      display: flex; flex-direction: column;
      .username { font-size: 14px; font-weight: 700; color: $text-main; }
      .mini-rate { transform: scale(0.8); transform-origin: left; margin-top: -5px; }
    }
    .rev-date { margin-left: auto; color: #ccc; font-size: 12px; }
  }

  .rev-content {
    font-size: 15px; line-height: 1.7; color: #444;
    margin: 15px 0;
    text-align: left; /* 确保文字绝对靠左 */
  }

  .admin-reply {
    margin-top: 20px; background: #F0F7FF; /* 淡淡的奶蓝色背景 */
    padding: 15px 20px; border-radius: 16px;
    .reply-label { font-size: 12px; font-weight: 800; color: #6AAFE6; display: block; margin-bottom: 6px; }
    p { color: #5a6b7d; font-size: 14px; margin: 0; }
  }
}

.no-reviews { padding: 40px 0; text-align: center; color: #ccc; font-size: 14px; }
</style>
