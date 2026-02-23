<template>
  <div class="product-page">
    <div class="container-width">

      <!-- 1. 筛选区 (设计成胶囊切换风格) -->
      <div class="filter-section">
        <!-- 国家筛选 -->
        <div class="filter-row">
          <span class="label">热门国家：</span>
          <div class="options">
            <span
              class="capsule"
              :class="{ active: !queryParams.countryId }"
              @click="handleFilter('countryId', null)"
            >全部</span>
            <span
              v-for="c in countryList" :key="c.id"
              class="capsule"
              :class="{ active: queryParams.countryId === c.id }"
              @click="handleFilter('countryId', c.id)"
            >
              {{ c.name }}
            </span>
          </div>
        </div>

        <!-- 类型筛选 -->
        <div class="filter-row mt-20">
          <span class="label">签证类型：</span>
          <div class="options">
            <span
              class="capsule"
              :class="{ active: !queryParams.typeId }"
              @click="handleFilter('typeId', null)"
            >全部</span>
            <span
              v-for="t in typeList" :key="t.id"
              class="capsule"
              :class="{ active: queryParams.typeId === t.id }"
              @click="handleFilter('typeId', t.id)"
            >
              {{ t.name }}
            </span>
          </div>
        </div>

        <!-- 领区筛选 -->
      <div class="filter-row mt-20">
        <span class="label">办签领区：</span>
        <div class="options">
          <span class="capsule" :class="{ active: !queryParams.districtId }"
            @click="handleFilter('districtId', null)">全部领区</span>
            <!-- 循环领区胶囊 -->
           <el-popover v-for="d in districtList" :key="d.id" placement="top" trigger="hover"
              :content="'覆盖省份：' + (d.coverArea || '全国通用')" :disabled="!d.coverArea">
              <span slot="reference" class="capsule" :class="{ active: queryParams.districtId === d.id }"
                @click="handleFilter('districtId', d.id)">
                {{ d.name }}
              </span>
            </el-popover>

        </div>
      </div>

      </div>

      <!-- 2. 产品列表 (使用 AppleCard) -->
      <div class="product-grid">
        <apple-card
          v-for="item in productList"
          :key="item.id"
          hover
          class="product-item"
          @click.native="goDetail(item.id)"
        >
          <div class="img-wrapper">
            <img :src="$getImgUrl(item.image)" loading="lazy">
            <div class="tag-price">￥{{ item.price }}</div>
          </div>
          <div class="info">
            <h3 class="title">{{ item.title }}</h3>
            <div class="tags">
              <el-tag size="mini" type="info">{{ item.countryName }}</el-tag>
              <el-tag size="mini" type="info">{{ item.typeName }}</el-tag>
              <el-tag size="mini" v-if="item.isInterviewRequired" type="warning">需面试</el-tag>
            </div>
            <p class="desc">办理时长：{{ item.processingTime }}</p>
          </div>
        </apple-card>
      </div>

      <div class="pagination-container" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="queryParams.pageSize"
        :current-page.sync="queryParams.pageNum"
        @current-change="handlePageChange"
      >
      </el-pagination>
    </div>

    </div>
  </div>
</template>

<script>
import AppleCard from '@/components/AppleCard'
import { listProduct } from '@/api/product'
import { listCountry, listType, listDistrict } from '@/api/common'

export default {
  components: { AppleCard },
  data () {
    return {
      queryParams: {
        countryId: null,
        typeId: null,
        districtId: null,
        pageNum: 1,
        pageSize: 12
      },
      countryList: [],
      typeList: [],
      productList: [],
      districtList: [],
      total: 0

    }
  },
  created () {
    console.log('【调试】ProductList 组件加载成功了！！！')
    this.initData()
    this.getList()
  },
  methods: {
    initData () {
      // 获取筛选条件
      listCountry().then(res => { this.countryList = res.data })
      listType().then(res => { this.typeList = res.data })
      listDistrict().then(res => { this.districtList = res.data })
    },
    getList () {
      listProduct(this.queryParams).then(res => {
        // ★ 修改：因为后端改成了 getDataTable，数据在 rows 里，总数在 total 里
        this.productList = res.rows
        this.total = res.total

        // 体验优化：翻页后自动回到顶部
        window.scrollTo({ top: 0, behavior: 'smooth' })
      })
    },
    // ★ 新增：翻页事件
    handlePageChange (val) {
      this.queryParams.pageNum = val
      this.getList()
    },

    // 筛选时记得重置到第一页
    handleFilter (key, val) {
      this.queryParams[key] = val
      this.queryParams.pageNum = 1 // ★ 重置页码
      this.getList()
    },
    goDetail (id) {
      this.$router.push(`/product/${id}`)
    }

  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

.product-page {
  padding-top: 40px;
  min-height: 80vh;
}

/* 筛选区样式 - 胶囊风格 */
.filter-section {
  background-color: #fff;
  // margin-bottom: 40px;
  // padding: 0 20px;
  padding: 30px;
  border-radius: 24px;
  margin-bottom: 40px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
}

.mt-20 {
  margin-top: 25px; // 稍微加大行间距，更有呼吸感
}

.filter-row {
  display: flex;
  align-items: center;
  .label { font-weight: 600; color: $text-main; margin-right: 15px; white-space: nowrap; }
  margin-bottom: 20px;
}
.capsule {
  display: inline-block;
  padding: 6px 16px;
  margin-right: 10px;
  border-radius: 20px;
  font-size: 14px;
  color: $text-secondary;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;

  &:hover { color: $primary-color; background: rgba(106, 175, 230, 0.1); }
  &.active {
    background: $primary-color;
    color: #fff;
    box-shadow: 0 4px 15px rgba(106, 175, 230, 0.4);
  }
}

/* 网格布局 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 一行4个 */
  gap: 30px;
}

.product-item {
  padding: 0 !important; /* 覆盖默认 padding */
  cursor: pointer;

  .img-wrapper {
    height: 160px;
    position: relative;
    img { width: 100%; height: 100%; object-fit: cover; }
    .tag-price {
      position: absolute; bottom: 10px; right: 10px;
      background: rgba(255,255,255,0.9);
      padding: 4px 10px;
      border-radius: 12px;
      font-weight: 700;
      color: #ff6b6b; /* 价格可以用醒目一点的颜色 */
      font-size: 16px;
    }
  }

  .info {
    padding: 20px;
    .title { font-size: 16px; margin-bottom: 10px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .tags { display: flex; gap: 5px; margin-bottom: 10px; }
    .desc { font-size: 12px; color: $text-secondary; }
  }

  .pagination-container {
  margin-top: 40px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

/* 深度选择器：修改 ElementUI 分页按钮的样式，让它变圆 */
::v-deep .el-pagination.is-background .el-pager li:not(.disabled).active {
  background-color: $primary-color; /* 使用我们的奶蓝色 */
  color: #fff;
  border-radius: 8px; /* 圆角 */
}

::v-deep .el-pagination.is-background .el-pager li {
  border-radius: 8px; /* 普通按钮也圆角 */
  background-color: #fff; /* 白底 */
  box-shadow: 0 2px 5px rgba(0,0,0,0.05); /* 微微阴影 */
}
}
</style>
