<template>
  <div class="upload-page container-width" v-if="order.id">
    <!-- 1. 顶部 Header -->
    <div class="page-header-bar">
      <div class="header-content">
        <div class="back-btn" @click="$router.back()"><i class="el-icon-arrow-left"></i> 返回</div>
        <div class="title-info">
          <h2>提交申请材料</h2>
          <p>订单号：{{ order.orderNo }} | 产品：{{ product.title }}</p>
        </div>
        <el-button type="primary" round class="submit-all-btn" :loading="submitting" @click="handleSubmit">
          全部提交审核
        </el-button>
      </div>
    </div>

    <div class="main-container">
      <!-- 2. 左侧：申请人切换列表 (Left Side) -->
      <div class="sidebar-wrapper">
        <div class="sidebar-title">申请人列表</div>
        <div v-for="(applicant, index) in applicantList" :key="applicant.id || index"
          :class="['applicant-nav-item', { active: activeIndex === index }]" @click="activeIndex = index">
          <div class="nav-left">
            <div class="nav-name">{{ applicant.name }}</div>
            <div class="nav-identity">{{ getIdentityLabel(applicant.identity) }}</div>
          </div>
          <div class="nav-right">
            <div class="progress-text">
              进度 <span>{{ getProgress(index, applicant.identity).filled }}/{{ getProgress(index,
                applicant.identity).total }}</span>
            </div>
            <!-- 进度圆点：全部传完显示绿色勾 -->
            <i v-if="isFinished(index, applicant.identity)" class="el-icon-success finished-icon"></i>
          </div>
        </div>

        <!-- 友情提醒 -->
        <div class="sidebar-tips">
          <i class="el-icon-info"></i>
          请务必切换每一位申请人，确保所有必填项均已完成。
        </div>
      </div>

      <!-- 3. 右侧：材料上传区 (Right Content) -->
      <div class="content-wrapper">
        <!-- 当前申请人状态提醒 -->
        <div v-if="getIndividualRemark(activeIndex)" class="individual-remark-box">
          <div class="remark-header"><i class="el-icon-warning"></i> 修改建议 (针对 {{ applicantList[activeIndex].name }})
          </div>
          <div class="remark-content">{{ getIndividualRemark(activeIndex).text }}</div>
          <el-button v-if="getIndividualRemark(activeIndex).file" type="warning" size="mini" plain round
            @click="downloadTemplate(getIndividualRemark(activeIndex).file, '备注附件')">查看附件</el-button>
        </div>

        <div class="current-applicant-info">
          正在为 <span>{{ applicantList[activeIndex].name }}</span> 上传材料
        </div>

        <el-form label-position="top">
          <!-- 循环该身份过滤后的材料 -->
          <div v-for="(config, idx) in getFilteredMaterials(applicantList[activeIndex].identity)" :key="idx"
            class="material-card">
            <div class="item-header">
              <div class="item-title">
                <span v-if="config.required" class="required-star">*</span>
                {{ idx + 1 }}. {{ config.name }}

                <!-- ★ 核心新增：材料类型标注 ★ -->
                <el-tag :type="getTypeTag(config.type)" size="mini" effect="plain" class="type-tag">
                  {{ getTypeText(config.type) }}
                </el-tag>
              </div>

              <div v-if="config.templateUrl" class="template-link">
                <el-button type="text" icon="el-icon-download"
                  @click="downloadTemplate(config.templateUrl, config.templateName)">下载模板</el-button>
              </div>
            </div>

            <p class="item-desc" v-if="config.desc">{{ config.desc }}</p>

            <!-- 分支 A: 上传类 (图片/文件) -->
            <div v-if="config.type === 'image' || config.type === 'file'" class="upload-zone">
              <div v-if="submitArray[activeIndex] && submitArray[activeIndex][config.name]" class="preview-area">
                <template v-if="isRealImage(submitArray[activeIndex][config.name])">
                  <el-image class="img-thumb" :src="$getImgUrl(submitArray[activeIndex][config.name])"
                    :preview-src-list="[$getImgUrl(submitArray[activeIndex][config.name])]"></el-image>
                </template>
                <template v-else>
                  <div class="file-thumb" @click="downloadTemplate(submitArray[activeIndex][config.name])">
                    <i class="el-icon-document"></i>
                    <span>{{ getFileName(submitArray[activeIndex][config.name]) }}</span>
                  </div>
                </template>
              </div>

              <el-upload :action="uploadUrl" :headers="headers"
                :on-success="(res) => handleUploadSuccess(res, activeIndex, config.name)" :show-file-list="false">
                <el-button size="small" :type="submitArray[activeIndex][config.name] ? 'default' : 'primary'" round>
                  {{ submitArray[activeIndex][config.name] ? '重新上传' : '点击上传' }}
                </el-button>
              </el-upload>
            </div>

            <!-- 分支 B: 填写类 (纯文本) -->
            <div v-else class="input-zone">
              <el-input v-model="submitArray[activeIndex][config.name]" :placeholder="'请输入' + config.name"
                clearable></el-input>
            </div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
/* eslint-disable */
import { getOrder, submitMaterials } from '@/api/order'
import { getProduct } from '@/api/product'

export default {
  data() {
    return {
      activeIndex: 0, // 当前选中的申请人索引
      order: {},
      product: {},
      applicantList: [],
      materialConfig: [],
      submitArray: [],
      submitting: false,
      uploadUrl: '/api/common/upload', 
      headers: { Authorization: 'Bearer ' + localStorage.getItem('Client-Token') }
    }
  },
  created() {
    this.init()
  },
  methods: {
    async init() {
      const orderId = this.$route.query.orderId
      const orderRes = await getOrder(orderId)
      this.order = orderRes.data
      this.applicantList = this.order.applicantList || []

      const prodRes = await getProduct(this.order.productId)
      this.product = prodRes.data
      this.materialConfig = JSON.parse(this.product.requirementsConfig || '[]')

      // 初始化数据结构
      if (this.order.submittedMaterials) {
        this.submitArray = JSON.parse(this.order.submittedMaterials)
      } else {
        this.submitArray = this.applicantList.map(() => ({}))
      }
    },

    // 过滤方法
    getFilteredMaterials(identity) {
      return this.materialConfig.filter(m => !m.targetGroup || m.targetGroup === 'ALL' || m.targetGroup === identity)
    },

    // 进度计算
    getProgress(index, identity) {
      const materials = this.getFilteredMaterials(identity)
      const data = this.submitArray[index] || {}
      const filled = materials.filter(m => data[m.name] && String(data[m.name]).trim() !== '').length
      return { filled, total: materials.length }
    },

    isFinished(index, identity) {
      const p = this.getProgress(index, identity)
      return p.total > 0 && p.filled === p.total
    },

    // 类型标签辅助函数
    getTypeText(type) {
      const map = { 'image': '请上传图片', 'file': '请上传PDF/文档', 'text': '请填写文字' }
      return map[type] || '输入项'
    },
    getTypeTag(type) {
      const map = { 'image': 'success', 'file': 'warning', 'text': 'info' }
      return map[type] || ''
    },

    getIdentityLabel(id) {
      const map = { 'EMPLOYED': '在职', 'STUDENT': '学生', 'CHILD': '学龄前', 'RETIRED': '退休', 'FREELANCE': '自由职业', 'UNEMPLOYED': '无业/主妇' }
      return map[id] || '申请人'
    },

    handleUploadSuccess(res, index, field) {
      if (res.code === 200) {
        this.$set(this.submitArray[index], field, res.fileName)
        this.$message.success('上传成功')
      }
    },

    handleSubmit() {
      // 提交前校验：检查所有人是否都填完了必填项
      for (let i = 0; i < this.applicantList.length; i++) {
        const p = this.getProgress(i, this.applicantList[i].identity)
        if (p.filled < p.total) {
          this.activeIndex = i // 自动切换到没填完的那个人
          this.$message.warning(`请完成申请人 ${this.applicantList[i].name} 的所有资料`)
          return
        }
      }

      this.submitting = true
      submitMaterials({ id: this.order.id, submittedMaterials: JSON.stringify(this.submitArray) }).then(() => {
        this.$message.success('资料已全部提交，请等待审核')
        this.$router.push('/user/orders')
      }).finally(() => { this.submitting = false })
    },

    // 原有文件相关辅助方法 (isRealImage, getFileName, downloadTemplate 等) 保持不变...
    isRealImage(url) {
      const ext = String(url).substring(String(url).lastIndexOf('.') + 1).toLowerCase()
      return ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)
    },
    getFileName(url) {
      return String(url).substring(String(url).lastIndexOf('/') + 1)
    },
    downloadTemplate(url) {
      let fullUrl = url.startsWith('http') ? url : ('http://localhost:8080' + url)
      window.open(fullUrl, '_blank')
    },
    getIndividualRemark(index) {
      if (!this.order.auditRemark) return null
      try {
        const remarks = JSON.parse(this.order.auditRemark)
        return remarks[index] || null
      } catch (e) { return null }
    }
  }
}
</script>

<style scoped lang="scss">
/* eslint-disable */
@import "@/assets/styles/variables.scss";

.upload-page {
  background: #F5F5F7;
  min-height: 100vh;
  padding: 0;
  display: block;
  padding-top: 70px; 
}

/* Header 样式 */
.page-header-bar {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  height: 70px;
  position: sticky;
  top: 70px; 
  z-index: 900; 
  border-bottom: 1px solid #eee;

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;

    .back-btn {
      cursor: pointer;
      color: #86868b;
      font-size: 14px;
    }

    .title-info {
      text-align: center;

      h2 {
        font-size: 18px;
        margin-bottom: 2px;
      }

      p {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

.main-container {
  display: flex;
  max-width: 1200px;
  margin: 30px auto;
  gap: 30px;
  padding: 0 20px;
}

/* Sidebar 样式 */
.sidebar-wrapper {
  width: 280px;
  flex-shrink: 0;

  .sidebar-title {
    font-size: 13px;
    font-weight: 700;
    color: #86868b;
    margin-bottom: 15px;
    padding-left: 10px;
  }
}

.applicant-nav-item {
  background: #fff;
  border-radius: 14px;
  padding: 15px;
  margin-bottom: 12px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;

  &:hover {
    transform: scale(1.02);
  }

  &.active {
    border-color: $primary-color;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  }

  .nav-name {
    font-weight: 700;
    font-size: 16px;
    color: #1d1d1f;
  }

  .nav-identity {
    font-size: 12px;
    color: #86868b;
    margin-top: 4px;
  }

  .progress-text {
    font-size: 11px;
    color: #999;
    text-align: right;

    span {
      color: $primary-color;
      font-weight: 700;
    }
  }

  .finished-icon {
    color: #34C759;
    font-size: 18px;
  }
}

/* Content 样式 */
.content-wrapper {
  flex: 1;
  min-width: 0;
}

.current-applicant-info {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 25px;

  span {
    color: $primary-color;
  }
}

.material-card {
  background: #fff;
  border-radius: 20px;
  padding: 25px;
  margin-bottom: 20px;

  .item-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 10px;
  }

  .item-title {
    font-size: 17px;
    font-weight: 700;
    color: #1d1d1f;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .required-star {
    color: #FF3B30;
  }

  .type-tag {
    font-weight: normal;
    border-radius: 6px;
  }

  .item-desc {
    font-size: 14px;
    color: #86868b;
    line-height: 1.5;
    margin-bottom: 20px;
  }
}

.upload-zone {
  background: #F5F5F7;
  border-radius: 12px;
  padding: 20px;
  text-align: center;

  .preview-area {
    margin-bottom: 15px;
  }

  .img-thumb {
    width: 140px;
    height: 100px;
    border-radius: 8px;
    border: 2px solid #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .file-thumb {
    background: #fff;
    padding: 10px;
    border-radius: 8px;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
  }
}

.sidebar-tips {
  margin-top: 30px;
  font-size: 12px;
  color: #999;
  line-height: 1.6;
  padding: 0 10px;
}
</style>