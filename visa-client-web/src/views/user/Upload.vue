<template>
  <div class="upload-page container-width" v-if="order.id">
    <div class="centered-content container-width">
    <div class="page-header">
      <h2>提交申请材料</h2>
      <p>订单号：{{ order.orderNo }} | 需提交 {{ order.quantity }} 份材料</p>
    </div>

    <!-- 顶部显著提醒 -->
    <apple-card v-if="order.status === 3" class="rejection-card mb-30">
      <div class="rejection-inner">
        <div class="icon-side">
          <i class="el-icon-error"></i>
        </div>
        <div class="text-side">
          <h4>材料审核未通过</h4>
           <p>您的申请材料被退回，请查看下方各申请人的具体修改建议。</p>
            <span class="sub">修正后重新提交即可。</span>
        </div>
      </div>
    </apple-card>

    <!-- 循环渲染申请人卡片 -->
    <div v-for="n in order.quantity" :key="n" class="applicant-section">
      <apple-card class="mb-30">
         <!-- ★★★ 核心新增：精准备注显示 ★★★ -->
          <div v-if="getIndividualRemark(n - 1)" class="individual-remark-box">
            <div class="remark-header">
              <i class="el-icon-warning"></i> 修改建议
            </div>
            <div class="remark-content" v-if="getIndividualRemark(n - 1).text">
              {{ getIndividualRemark(n - 1).text }}
            </div>
            <!-- ★★★ 核心新增：如果管理员给了文件，显示下载区 ★★★ -->
           <div v-if="getIndividualRemark(n - 1).file" class="admin-file-box">
              <el-button type="warning" size="mini" round icon="el-icon-paperclip"
                @click="downloadTemplate(getIndividualRemark(n - 1).file, '管理员附件')">
                查看管理员附件
              </el-button>
            </div>
          </div>

        <div class="applicant-header">
          <span class="badge">申请人 {{ n }}</span>
          <span class="tip">请确保该位申请人的信息真实准确</span>
        </div>

        <el-form label-position="top">
          <!-- 每一个申请人，都要循环渲染产品配置的材料项 -->
          <div v-for="(config, idx) in materialConfig" :key="idx" class="material-item">
            <label class="item-label">
              <span v-if="config.required" style="color:red">*</span> {{ config.name }}
              <span class="item-desc" v-if="config.desc">({{ config.desc }})</span>

              <!-- 模板下载链接 -->
              <div v-if="config.templateUrl" class="template-action">
                <el-button type="text" icon="el-icon-download"
                  @click="downloadTemplate(config.templateUrl, config.templateName)">
                  下载模板
                </el-button>
              </div>
            </label>
            <!-- ================== 分支 A: 图片或文件 ================== -->
            <div v-if="config.type === 'image' || config.type === 'file'" class="upload-container">

              <!-- 1. 已上传内容的预览区 -->
              <!-- 注意：这里加了 v-if，只有当 submitArray[n-1] 里有这个字段值时才显示 -->
              <div v-if="submitArray[n - 1][config.name]" class="current-file-preview">

                <!-- A1. 如果是图片：显示缩略图 -->
               <template v-if="isRealImage(submitArray[n - 1][config.name], config.type)">

                  <el-image class="preview-img" :src="$getImgUrl(submitArray[n - 1][config.name])"
                    :preview-src-list="[$getImgUrl(submitArray[n - 1][config.name])]">
                    <!-- 加一个加载失败的插槽，万一图片真的坏了，显示好看点 -->
                    <div slot="error" class="image-slot">
                      <i class="el-icon-picture-outline"></i>
                    </div>
                  </el-image>
                  <div class="preview-tip">已上传图片</div>
                </template>

                <!-- A2. 如果是文件：显示点击下载 -->
                <template v-else>
                  <!-- 点击下载/预览 -->
                  <div class="file-tag" @click="downloadTemplate(submitArray[n - 1][config.name], '查看文件')">
                    <i class="el-icon-document"></i>
                    <span>{{ getFileName(submitArray[n - 1][config.name]) }} (点击预览)</span>
                  </div>
                </template>
              </div>

              <!-- 2. 上传按钮 (始终显示，用于覆盖上传) -->
              <el-upload :action="uploadUrl" :headers="headers"
                :on-success="(res) => handleUploadSuccess(res, n - 1, config.name)" :show-file-list="false"
                class="apple-uploader">
                <el-button size="small" type="primary" plain round icon="el-icon-upload2">
                  {{ submitArray[n - 1][config.name] ? '更换' : '上传' }}
                </el-button>
              </el-upload>
            </div>

            <!-- ================== 分支 B: 纯文本输入 ================== -->
            <!-- 这里的 v-else 对应上面的 v-if(image/file) -->
            <div v-else>
              <el-input v-model="submitArray[n - 1][config.name]" placeholder="请填写信息"></el-input>
            </div>
            </div>

        </el-form>
      </apple-card>
    </div>

    <div class="footer-actions">
      <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleSubmit">
        确认并提交审核
      </el-button>
    </div>
  </div>
  </div>
</template>

<script>
import AppleCard from '@/components/AppleCard'
import { getOrder, submitMaterials } from '@/api/order'
import { getProduct } from '@/api/product'

export default {
  components: { AppleCard },
  data () {
    return {
      order: {},
      product: {},
      materialConfig: [], // 后台配的规则
      submitArray: [], // ★ 核心数据结构：[ {材料1:url, 材料2:text}, {人2数据}... ]
      submitting: false,
      uploadUrl: '/api/common/upload',
      headers: { Authorization: 'Bearer ' + localStorage.getItem('Client-Token') }
    }
  },
  created () {
    this.init()
  },
  methods: {
    async init () {
      const orderId = this.$route.query.orderId
      // 1. 获取订单详情
      const orderRes = await getOrder(orderId)
      this.order = orderRes.data

      // 2. 根据订单里的 productId 获取产品配置
      const prodRes = await getProduct(this.order.productId)
      this.product = prodRes.data
      this.materialConfig = JSON.parse(this.product.requirementsConfig || '[]')

      // 3. 回显数据逻辑
      if (this.order.submittedMaterials) {
        // 如果已经有资料了（比如从状态3回来的），直接解析并赋值
        this.submitArray = JSON.parse(this.order.submittedMaterials)
        console.log('检测到已有资料，已自动回显', this.submitArray)
      } else {
        // 如果是第一次上传，初始化空对象数组
        this.submitArray = []
        for (let i = 0; i < this.order.quantity; i++) {
          this.submitArray.push({})
        }
      }
      this.$forceUpdate()
    },

    handleSubmit () {
      this.submitting = true
      const postData = {
        id: this.order.id,
        // 将整个数组转成 JSON 字符串存入 submitted_materials
        submittedMaterials: JSON.stringify(this.submitArray)
      }

      submitMaterials(postData).then(res => {
        this.$message.success('资料提交成功，请耐心等待初审')
        this.$bus.$emit('order-updated') // 刷新导航栏数字
        this.$router.push('/user/orders')
      }).finally(() => {
        this.submitting = false
      })
    },

    // 下载/预览文件通用方法
    downloadTemplate (url, name) {
      if (!url) return

      // 1. 若依返回的是 /profile/upload... 这种相对路径
      // 我们需要拼上后端地址。假设你的后端在 localhost:8080
      // 如果你配置了 proxy /api，也可以用 /api + url
      let fullUrl = url

      // 如果路径不包含 http，说明是本地文件，需要拼接
      if (url.indexOf('http') === -1) {
        // 简单粗暴法：直接拼后端端口
        fullUrl = 'http://localhost:8080' + url
      }

      // 2. 触发下载/预览
      const link = document.createElement('a')
      link.href = fullUrl
      link.target = '_blank' // 新窗口打开，PDF会预览，Word会下载
      link.setAttribute('download', name || '文件') // 尝试触发下载
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },

    // 上传成功回调 (这里一定要用 $set，否则 Vue 检测不到变化)
    handleUploadSuccess (res, userIndex, fieldName) {
      if (res.code === 200) {
        // 这里的 res.fileName 就是 /profile/upload/...
        // 使用 $set 确保试图更新
        this.$set(this.submitArray[userIndex], fieldName, res.fileName)
        this.$forceUpdate() // 双重保险
      }
    },

    // 辅助：从路径中截取文件名显示
    getFileName (url) {
      if (!url) return '未知文件'
      // 截取最后斜杠后面的内容，且防止名字太长
      const name = url.substring(url.lastIndexOf('/') + 1)
      return name.length > 15 ? name.substring(0, 15) + '...' : name
    },
    isRealImage (url, configType) {
      if (!url) return false

      // 1. 获取文件后缀 (转小写)
      const ext = url.substring(url.lastIndexOf('.') + 1).toLowerCase()
      const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']

      // 2. 如果后缀在图片列表中，或者是 blob 预览流，认为是图片
      if (imageExts.includes(ext) || url.startsWith('blob:')) {
        return true
      }

      // 3. 如果后缀不是图片（比如 pdf, doc），那肯定不是图片
      // 4. 如果没有后缀（极少见），则回退参考后台配置
      return false
    },

    // 获取第 index 个人的具体备注
    // 修改 Upload.vue 里的方法
    getIndividualRemark (index) {
      if (!this.order.auditRemark) return null
      try {
        const remarks = JSON.parse(this.order.auditRemark)
        const item = remarks[index]
        if (item) {
          // 兼容老数据
          if (typeof item === 'string') return { text: item, file: '' }

          // ★★★ 核心修复：如果文字和文件都为空，返回 null，这样 v-if 就会隐藏盒子 ★★★
          if (!item.text && !item.file) return null

          return item
        }
        return null
      } catch (e) {
        return null
      }
    }

  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

.applicant-section { margin-bottom: 20px; }
/* 1. 核心容器：限制最大宽度并居中 */
.upload-page {
  padding: 60px 0 120px;
  background-color: $bg-color;
  display: flex;
  flex-direction: column;
  align-items: center; /* 确保子元素水平居中 */
}

.centered-content {
  width: 100%;
  max-width: 800px; /* ★ 关键：限制宽度，模拟 Apple 的设置/表单页面 */
}

/* 2. 页面头部居中 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  h2 { font-size: 30px; font-weight: 700; color: $text-main; margin-bottom: 12px; }
  p { color: $text-secondary; font-size: 15px; }
}

/* 3. 申请人卡片样式 */
.applicant-section {
  margin-bottom: 30px;
  width: 100%;
}

.applicant-header {
  display: flex;
  flex-direction: column; /* 改为垂直排列更显精致 */
  align-items: center;
  margin-bottom: 30px;
  .badge {
    background: linear-gradient(135deg, $primary-color 0%, $primary-hover 100%);
    color: #fff;
    padding: 6px 20px;
    border-radius: 50px; /* 胶囊状 */
    font-weight: 700;
    font-size: 14px;
    box-shadow: 0 4px 12px rgba(106, 175, 230, 0.3);
    margin-bottom: 10px;
  }
  .tip { font-size: 13px; color: #999; }
}

/* 4. 表单项居中对齐处理 */
.material-item {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #f0f2f5;
  &:last-child { border-bottom: none; }

  /* 标签行：保持左对齐或分散对齐 */
  .label-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }
}

/* 上传容器：内部元素可以水平排列并居中 */
.upload-container {
  display: flex;
  flex-direction: column; /* 竖向排列 */
  align-items: center;    /* ★ 核心：上传按钮和预览图居中 */
  justify-content: center;
  gap: 15px;
  background: #f8fafc;
  padding: 30px;
  border-radius: 20px;
  border: 2px dashed #e2e8f0;
}

/* 5. 底部按钮居中 */
.footer-actions {
  margin-top: 50px;
  display: flex;
  justify-content: center;
  .submit-btn {
    width: 100%;
    max-width: 400px;
    height: 56px;
    font-size: 18px;
    font-weight: 700;
    border-radius: 28px !important;
    box-shadow: 0 10px 20px rgba(106, 175, 230, 0.3);
  }
}

/* 模板下载按钮 */
.template-action {
  .el-button--text {
    padding: 0; font-size: 13px; color: $primary-color; font-weight: 600;
    &:hover { opacity: 0.8; text-decoration: underline; }
  }
}

/* 红色驳回卡片样式 */
.rejection-card {
  width: 745px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #FF7875 0%, #F5222D 100%) !important;
  color: #fff;
  .rejection-inner { display: flex; align-items: center; gap: 20px; }
  .icon-side i { font-size: 40px; }
  .text-side {
    h4 { font-size: 18px; margin-bottom: 5px; }
    p { font-size: 15px; margin-bottom: 5px; opacity: 0.9; }
    .sub { font-size: 12px; opacity: 0.7; }
  }
}

.current-file-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  .preview-img {
    width: 120px; height: 90px; /* 稍微调大一点 */
    border-radius: 12px; border: 3px solid #fff;
    box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  }
}

/* 文件预览标签样式 */
.file-tag {
  background: rgba(106, 175, 230, 0.1);
  color: #6AAFE6;
  padding: 8px 15px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer; /* 鼠标变手型 */
  display: flex; align-items: center; gap: 5px;
  transition: all 0.3s;

  &:hover {
    background: rgba(106, 175, 230, 0.2);
    transform: translateY(-2px);
  }
}

.apple-uploader {
  ::v-deep .el-button--primary.is-plain {
    background: transparent; border-color: #eee; color: #909399;
    &:hover { border-color: #6AAFE6; color: #6AAFE6; }
  }
}

.individual-remark-box {
  background-color: #FFFBE6; /* 柔和黄 */
  border: 1px solid #FFE58F;
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 25px;
  text-align: left;

  .remark-header {
    color: #856404;
    font-weight: 700;
    font-size: 14px;
    margin-bottom: 6px;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .remark-content {
    color: #856404;
    font-size: 14px;
    line-height: 1.6;
  }
}

.admin-file-box {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #FFE58F;

  .el-button--warning {
    background-color: #FFF;
    border-color: #FFE58F;
    color: #856404;
    &:hover {
      background-color: #FFFBE6;
    }
  }
}
</style>
