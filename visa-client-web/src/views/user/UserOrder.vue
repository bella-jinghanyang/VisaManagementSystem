<template>
  <div class="order-list-page container-width">
    <div class="page-header">
      <h2>我的订单</h2>
      <p>管理您的签证申请进度</p>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-box">
      <i class="el-icon-loading"></i> 加载中...
    </div>

    <!-- 有数据 -->
    <div v-else-if="orderList.length > 0" class="order-list">
      <apple-card v-for="order in orderList" :key="order.id" class="order-item mb-20">
        <!-- 1. 上半部分：订单基本信息 -->
        <div class="order-body">
          <div class="img-box">
            <img :src="$getImgUrl(parseSnapshot(order.productSnapshot).image)" class="prod-img">
          </div>

          <div class="order-info">
            <div class="top-row">
              <span class="order-no">单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ order.createTime }}</span>
            </div>
            <h3 class="title">{{ parseSnapshot(order.productSnapshot).title }}</h3>
            <div class="price-row">
              <span class="qty">{{ order.quantity }} 人办理</span>
              <span class="money">￥{{ order.totalAmount }}</span>
            </div>
          </div>

          <!-- 右上角仅保留状态标签和驳回原因 -->
          <div class="status-top-right">
            <div class="status-wrapper">
              <div :class="['status-badge', 'status-' + order.status]">
                {{ statusMap[order.status] || '未知状态' }}
              </div>
              <!-- 驳回原因气泡 -->
              <el-popover v-if="order.status === 3" placement="top" title="修改意见" width="200" trigger="hover"
                :content="order.auditRemark || '请核对材料后重新上传。'">
                <i slot="reference" class="el-icon-warning remark-icon"></i>
              </el-popover>
            </div>
          </div>
        </div>

        <!-- ★★★ 新增：面试时间选择胶囊 ★★★ -->
        <!-- 逻辑：状态为 7(待预约) 且 管理员发了方案 且 用户还没选定最终时间 时显示 -->
        <div v-if="order.status === 7 && order.interviewSlots && !order.interviewTime" class="slot-selection-card">
          <div class="notice-header">
            <i class="el-icon-date"></i> 请选择您的面试意向时间
          </div>
          <p class="notice-tip">管理员已为您争取到以下可选时段，请选择一个最方便的：</p>

          <div class="slots-grid">
            <div v-for="(time, index) in parseJson(order.interviewSlots)" :key="index" class="time-capsule"
              @click="handleConfirmTime(order, time)">
              <div class="date-text">{{ time.split(' ')[0] }}</div>
              <div class="time-text">{{ time.split(' ')[1] }}</div>
            </div>
          </div>

          <div class="chat-hint">
            时间都不合适？ <el-button type="text" @click="handleOpenChat(order)">点击留言告知客服</el-button>
          </div>
        </div>

        <!-- 状态 8：显示最终确定的面试信息 -->
        <div v-if="order.status === 8" class="interview-final-notice">
          <div class="final-header"><i class="el-icon-message-solid"></i> 面试预约已就绪</div>
          <div class="final-body">
            <p><span>面试时间：</span><strong>{{ order.interviewTime }}</strong></p>
            <p><span>面试地点：</span><strong>{{ order.interviewLocation || '请下载预约单查看详情' }}</strong></p>

            <div class="final-actions">
              <el-button v-if="order.interviewFile" type="warning" size="mini" round icon="el-icon-download"
                @click="downloadTemplate(order.interviewFile, '面试确认单')">
                下载预约确认单
              </el-button>
            </div>
          </div>
        </div>

        <!-- ★★★ 修改后的办理结果与物流区（状态4, 5, 6均显示） ★★★ -->
        <div v-if="order.status >= 4 && order.visaResult" class="final-result-card">
          <div class="result-header">
            <span class="label">办理进度明细：</span>
          </div>

          <!-- 1. 分人结果列表 -->
          <div v-for="(res, idx) in parseJson(order.visaResult)" :key="idx" class="person-res-row">
            <div class="res-left">
              <span class="p-name">申请人 {{ idx + 1 }}: </span>
              <el-tag :type="getResultTagType(res.status)" size="mini" round>
                {{ getResultText(res.status) }}
              </el-tag>

              <!-- 已传补件预览 -->
              <div v-if="getSuppFileList(order, idx).length > 0" class="quick-preview-box">
                <el-link v-for="(fileUrl, fIdx) in getSuppFileList(order, idx)" :key="fIdx" type="primary"
                  :underline="false" class="mini-file-link" @click="downloadTemplate(fileUrl, '补交材料')">
                  <i class="el-icon-document"></i> 附件{{ fIdx + 1 }}
                </el-link>
              </div>

              <!-- 管理员备注 -->
              <span class="res-memo" v-if="res.memo">({{ res.memo }})</span>
            </div>

            <!-- 如果被 Check (状态3)，显示补件按钮 -->
            <div class="res-right" v-if="res.status == 3">
              <el-button type="primary" size="mini" icon="el-icon-plus" @click="handleUploadSupplementary(order, idx)">
                补交材料
              </el-button>
            </div>
          </div>

          <!-- 2. 物流信息 (仅在有单号时显示) -->
          <div class="shipping-box" v-if="order.trackingNumber" style="margin-top: 15px;">
            <div class="ship-info">
              <i class="el-icon-truck"></i>
              <span>护照已通过顺丰寄回：<strong>{{ order.trackingNumber }}</strong></span>
            </div>
          </div>

          <!-- 3. 全员拒签的特殊提示 -->
          <div v-else-if="order.status === 6" class="no-shipping-tip" style="margin-top: 15px; font-size: 13px; color: #909399;">
            <i class="el-icon-info"></i> 本订单已结案。由于未产生过签护照，无回寄物流。
          </div>
        </div>

        <!-- 2. 底部工具栏 (核心修改) -->
        <div class="order-footer">
          <div class="footer-left">
            <!-- 预留位置，可以放个图标或者留空 -->
          </div>

          <!-- 底部中间：主操作按钮 -->
          <div class="footer-center">
            <!-- 状态 0: 立即支付 -->
            <el-button v-if="order.status === 0" type="primary" size="medium" class="main-action-btn"
              @click="handlePay(order)">立即支付</el-button>

            <!-- 状态 1: 首次上传材料 -->
            <el-button v-if="order.status === 1" type="success" size="medium" class="main-action-btn"
              icon="el-icon-upload2" @click="handleUpload(order)">上传材料</el-button>

            <!-- 状态 3: 需补交材料 (补上这个按钮！) -->
            <el-button v-if="order.status === 3" type="warning" size="medium" class="main-action-btn"
              icon="el-icon-edit" @click="handleUpload(order)">修改并重传</el-button>

            <!-- 状态 8：面试完录入面试结果-->
            <el-button v-if="order.status === 8" type="success" size="medium" class="main-action-btn"
              icon="el-icon-circle-check" @click="handleInterviewFinish(order)">我已面试完</el-button>

            <!-- ★★★ 状态 5：确认收货 ★★★ -->
            <el-button v-if="order.status === 5" type="primary" size="medium" class="main-action-btn"
              icon="el-icon-check" @click="handleConfirmReceipt(order)">确认收到护照</el-button>

            <!-- 状态 6：已完成 -->
            <span v-if="order.status === 6" class="finished-text">
              服务已圆满完成 <i class="el-icon-finished"></i>
            </span>
            <!-- 其他进行中的提示文字 -->
            <span v-if="[2, 4, 7].includes(order.status)" class="waiting-text">
              <i class="el-icon-loading"></i> {{ statusMap[order.status] }}中...
            </span>
          </div>

          <!-- 底部右侧：次要操作 -->
          <div class="footer-right">
            <el-button type="text" icon="el-icon-right" @click="goDetail(order.id)">查看进度</el-button>
          </div>
        </div>

      </apple-card>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <apple-card>
        <i class="el-icon-document-remove"></i>
        <p>您还没有相关的订单</p>
        <el-button type="primary" round @click="$router.push('/products')">去签证大厅看看</el-button>
      </apple-card>
    </div>

    <!-- 面试结果反馈弹窗 -->
    <el-dialog title="面试结果快速反馈" :visible.sync="feedbackOpen" width="500px" append-to-body custom-class="apple-dialog">
      <div class="feedback-container" v-if="currentOrder.id">
        <p class="feedback-tip"><i class="el-icon-info"></i> 请根据使馆现场反馈，录入每位申请人的结果：</p>

        <!-- 循环每一位申请人 -->
        <div v-for="(person, idx) in feedbackList" :key="idx" class="person-feedback-row">
          <div class="p-name">申请人 {{ idx + 1 }}</div>
          <el-radio-group v-model="person.result" size="small">
            <el-radio-button :label="1">过签</el-radio-button>
            <el-radio-button :label="2">拒签</el-radio-button>
            <el-radio-button :label="3">被Check</el-radio-button>
          </el-radio-group>
        </div>

        <el-divider></el-divider>

        <!-- 上传凭证（单子照片） -->
        <div class="upload-slip">
          <div class="label">上传使馆通知单 (可选)</div>
          <el-upload action="/api/common/upload" :headers="headers" :on-success="handleSlipSuccess" :limit="1"
            list-type="picture">
            <el-button size="small" icon="el-icon-camera">点击拍照或上传</el-button>
          </el-upload>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="feedbackOpen = false" round>取消</el-button>
        <el-button type="primary" @click="submitFeedback" round :loading="loading">提交反馈</el-button>
      </div>
    </el-dialog>

    <!-- 补充材料上传弹窗 (针对 Check) -->
    <el-dialog :title="'为 申请人' + (targetPersonIdx + 1) + ' 提交补充材料'" :visible.sync="suppOpen" width="450px"
      append-to-body>
      <div class="supp-container">
        <div class="supp-tips">
          <i class="el-icon-info"></i>
          请上传使馆要求的补充文件（如简历、行程单、导师信息等）。
        </div>

        <!-- 上传组件 -->
        <el-upload class="supp-uploader" action="/api/common/upload" :headers="headers" :on-success="handleSuppSuccess"
          :file-list="tempFileList" :on-remove="handleSuppRemove" :on-change="handleFileChange" :on-preview="handleSuppPreview" multiple list-type="text">
          <el-button size="small" type="primary" icon="el-icon-upload">点击选择文件</el-button>
        </el-upload>
      </div>

      <div slot="footer">
        <el-button @click="suppOpen = false" round>取消</el-button>
        <el-button type="primary" @click="submitSupplementary" round :loading="loading">确认提交</el-button>
      </div>
    </el-dialog>
  </div>

</template>

<script>
import AppleCard from '@/components/AppleCard'
import { listMyOrders } from '@/api/order'
import request from '@/utils/request'

export default {
  name: 'UserOrder',
  components: { AppleCard },
  data () {
    return {
      loading: false,
      orderList: [],
      currentOrder: {},
      // 状态字典
      statusMap: {
        0: '待支付',
        1: '待上传材料',
        2: '审核中',
        3: '需补交材料',
        4: '办理中',
        5: '待收货',
        6: '已完成',
        7: '材料过审，待预约面试',
        8: '待面试'
      },
      feedbackOpen: false,
      feedbackList: [], // 存储多人的结果
      slipUrl: '', // 存凭证图
      tempFileList: [], // 临时存储本次上传的文件
      suppOpen: false, // 控制补件弹窗
      targetPersonIdx: 0, // 正在为第几个人补件
      headers: { Authorization: 'Bearer ' + localStorage.getItem('Client-Token') }
    }
  },
  created () {
    this.getList()
  },
  methods: {
    // 获取列表
    getList () {
      this.loading = true
      const user = JSON.parse(localStorage.getItem('Client-User'))
      listMyOrders(user.id).then(res => {
        this.orderList = res.data
      }).finally(() => {
        this.loading = false
      })
    },

    // 解析快照 JSON
    parseSnapshot (jsonStr) {
      if (!jsonStr) return {}
      try {
        return JSON.parse(jsonStr)
      } catch (e) {
        return {}
      }
    },

    // ★★★ 核心：唤起支付宝支付 ★★★
    // UserOrder.vue -> methods
    handlePay (order) {
      const loading = this.$loading({
        lock: true,
        text: '正在跳转支付...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      request({
        url: '/client/alipay/pay',
        method: 'get',
        params: { orderNo: order.orderNo }
      }).then(res => {
        loading.close()

        // res 是若依返回的标准格式：{ code: 200, msg: "下单成功", data: "<form..." }
        if (res.code === 200) {
          // ★★★ 核心修复点：取 res.data 而不是 res.msg ★★★
          const formHtml = res.data

          if (!formHtml || formHtml.indexOf('<form') === -1) {
            this.$message.error('后端返回的支付表单格式不正确')
            return
          }

          // 创建一个临时容器
          const div = document.createElement('div')
          div.innerHTML = formHtml // 这时 div 里就有了那个表单
          document.body.appendChild(div)

          // ★★★ 核心修复点：强制触发表单提交 ★★★
          // 因为 innerHTML 插入的 <script> 不会自动执行，我们手动点一下
          this.$nextTick(() => {
            // 找到刚刚插入的那个名字叫 punchout_form 的表单
            const payForm = document.getElementsByName('punchout_form')[0]
            if (payForm) {
              payForm.submit()
            } else {
              // 如果名字不对，就提交页面上最后一个表单
              document.forms[document.forms.length - 1].submit()
            }
          })
        } else {
          this.$message.error(res.msg || '支付发起失败')
        }
      }).catch(err => {
        console.error(err) // 或上报 Sentry / 记录日志
        this.$message.error('系统故障，请联系管理员')
      }).finally(() => {
        loading.close()
      })
    },

    // 跳转上传页 (下一步要做这个页面)
    handleUpload (order) {
      this.$router.push(`/order/upload?orderId=${order.id}`)
    },

    // 跳转详情
    goDetail (id) {
      this.$router.push(`/order/detail/${id}`) // 还没做，先预留
    },

    // 判断是否是 JSON 数组字符串
    isJsonArray (str) {
      if (!str) return false
      return typeof str === 'string' && str.startsWith('[')
    },

    /** 1. 解析 JSON 字符串 */
    parseJson (str) {
      if (!str) return []
      try {
        return JSON.parse(str)
      } catch (e) {
        return []
      }
    },

    /** 2. 用户点击胶囊确认时间 */
    handleConfirmTime (order, selectedTime) {
      this.$confirm(`确认选择 ${selectedTime} 参加面试吗？`, '面试时间确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
        roundButton: true
      }).then(() => {
        // 准备更新数据
        const data = {
          id: order.id,
          interviewTime: selectedTime, // 确定最终时间
          interviewSlots: null, // 清空备选列表，设为空字符串
          status: 8 // 状态流转到 8：待面试
        }

        // ★★★ 核心修复：使用 request 访问刚才新建的 C端接口 ★★★
        request({
          url: '/client/order/update',
          method: 'put',
          data: data
        }).then(res => {
          this.$message.success('预约意向确认成功！')
          this.getList() // 刷新列表
          this.$bus.$emit('order-updated') // 刷新导航栏数字
        }).catch(err => {
          console.error('更新失败：', err)
        })
      }).catch(() => { })
    },

    /** 3. 下载面试确认单 */
    downloadFile (url) {
      if (!url) return
      window.open('http://localhost:8080' + url, '_blank')
    },

    /** 4. 用户反馈面试完成 */
    /** 1. 点击“我已面试完”按钮触发 */
    handleInterviewFinish (order) {
      this.currentOrder = order
      // 根据订单份数初始化反馈数组
      this.feedbackList = []
      for (let i = 0; i < order.quantity; i++) {
        this.feedbackList.push({ result: 1 }) // 默认选“过签”
      }
      this.slipUrl = ''
      this.feedbackOpen = true
    },

    /** 2. 凭证上传成功 */
    handleSlipSuccess (res) {
      this.slipUrl = res.fileName
      this.$message.success('凭证上传成功')
    },

    /** 3. 提交反馈到后端 */
    submitFeedback () {
      // 不要用 this.loading = true，否则列表会消失
      // 如果非要用，记得最后要把 loading 变回 false

      const data = {
        id: this.currentOrder.id,
        status: 4,
        interviewFeedback: JSON.stringify(this.feedbackList),
        remark: '用户已自主反馈面试结果。凭证路径：' + this.slipUrl
      }

      request({
        url: '/client/order/update',
        method: 'put',
        data: data
      }).then(res => {
        this.$message.success('反馈已收到，我们会尽快为您核实结果')
        this.feedbackOpen = false
        this.getList() // 重新加载列表，这时 loading 会自动处理
      })
    },

    /** 5. 下载/预览确认单 */
    downloadTemplate (url, name) {
      if (!url) return
      // 直接新窗口打开
      window.open('http://localhost:8080' + url, '_blank')
    },

    handleConfirmReceipt (order) {
      this.$confirm('请确认您已收到快递并核对护照原件。确认后订单将正式结案。', '确认收货', {
        confirmButtonText: '确认收到',
        cancelButtonText: '取消',
        type: 'success'
      }).then(() => {
        // 状态流转到 6: 已完成
        const data = {
          id: order.id,
          status: 6
        }
        request({
          url: '/client/order/update',
          method: 'put',
          data: data
        }).then(() => {
          this.$message.success('订单已完成，祝您旅途愉快！')
          this.getList() // 刷新
          this.$bus.$emit('order-updated') // 导航栏数字 -1
        })
      })
    },

    /** ★★★ 新增：点击上传补充材料按钮 ★★★ */
    // handleUploadSupplementary (order, personIndex) {
    //   // 这个功能是为了应对 Check 情况，引导用户去专门的补件页面或弹窗
    //   // 建议：跳转到我们之前做的 Upload.vue，并在路径里带上标记
    //   this.$router.push({
    //     path: '/order/upload',
    //     query: {
    //       orderId: order.id,
    //       isSupplementary: true, // 告诉上传页：这是补件，不是初审
    //       personIndex: personIndex // 告诉上传页：是给第几个人补件
    //     }
    //   })
    // },

    /** ★★★ 新增：打开留言板 ★★★ */
    handleOpenChat (order) {
      // 跳转到 AI 助手或者留言页 (根据你之前的逻辑)
      this.$router.push(`/ai-chat?orderId=${order.id}`)
    },

    /** 1. 核心：监听文件列表变化 */
    handleFileChange (file, fileList) {
    // 只要文件列表变了（添加、上传成功、删除），就同步给 tempFileList
      this.tempFileList = fileList
    },

    openSuppDialog (order, index) {
      this.currentOrder = order
      this.targetPersonIdx = index

      // ★ 1. 先清空临时列表
      this.tempFileList = []

      // ★ 2. 如果之前已经有补件记录了，要把旧的加载出来（回显）
      if (order.supplementaryMaterials) {
        try {
          const allData = JSON.parse(order.supplementaryMaterials)
          const myFiles = allData[index] || []
          // 转换成 el-upload 认识的格式
          this.tempFileList = myFiles.map(url => ({
            name: url.substring(url.lastIndexOf('/') + 1),
            url: url
          }))
        } catch (e) { this.tempFileList = [] }
      }

      this.suppOpen = true
    },

    /** 获取某位申请人的补件文件数组（用于回显） */
    getSuppFileList (order, idx) {
      if (!order.supplementaryMaterials) return []
      try {
        const allData = JSON.parse(order.supplementaryMaterials)
        return allData[idx.toString()] || [] // 根据索引拿数组
      } catch (e) { return [] }
    },

    /** 打开补件弹窗并实现“回显” */
    handleUploadSupplementary (order, idx) {
      this.currentOrder = order
      this.targetPersonIdx = idx

      // --- 回显逻辑：从数据库读取已有的文件 ---
      const existFiles = this.getSuppFileList(order, idx)
      // 将路径字符串数组转为 el-upload 需要的对象数组格式
      this.tempFileList = existFiles.map(path => ({
        name: path.substring(path.lastIndexOf('/') + 1), // 截取文件名
        url: path, // 供组件展示
        fileName: path // 供提交时提取
      }))

      this.suppOpen = true
    },

    /** 处理补件上传成功 */
    handleSuppSuccess (res, file, fileList) {
      if (res.code === 200) {
        file.fileName = res.fileName // 存入后端返回的路径
        this.tempFileList = fileList
        this.$message.success('上传成功')
      }
    },

    /** 处理补件删除 */
    handleSuppRemove (file, fileList) {
      this.tempFileList = fileList
    },

    /** 提交补充材料（保存到数据库） */
    submitSupplementary () {
      // 提取所有文件的真正路径
      const finalPaths = this.tempFileList.map(f => {
        return f.response ? f.response.fileName : (f.fileName || f.url)
      })

      if (finalPaths.length === 0) return this.$message.warning('请至少上传一个文件')

      this.loading = true
      let allSuppData = {}
      try {
        allSuppData = JSON.parse(this.currentOrder.supplementaryMaterials || '{}')
      } catch (e) { allSuppData = {} }

      // 更新当前这位申请人的补件列表
      allSuppData[this.targetPersonIdx.toString()] = finalPaths

      const data = {
        id: this.currentOrder.id,
        supplementaryMaterials: JSON.stringify(allSuppData),
        remark: `申请人${this.targetPersonIdx + 1}已更新补充材料`
      }

      request({
        url: '/client/order/update',
        method: 'put',
        data: data
      }).then(res => {
        this.$message.success('补充材料已同步成功')
        this.suppOpen = false
        this.getList() // 刷新列表，看到“已传n份”
      }).finally(() => { this.loading = false })
    },

    /** 翻译结果标签和文字 */
    getResultTagType (status) {
      if (status === 1) return 'success' // 绿色
      if (status === 3) return 'warning' // 黄色
      return 'danger' // 红色
    },
    getResultText (status) {
      if (status === 1) return '已出签'
      if (status === 3) return '审查中(Check)'
      return '已拒签'
    },
    /** ★★★ 新增：处理补件列表中的点击预览 ★★★ */
    handleSuppPreview (file) {
      // 1. 获取文件路径
      // 如果是新上传的，路径在 file.response.fileName
      // 如果是回显的，路径在 file.fileName 或 file.url
      const path = file.response ? file.response.fileName : (file.fileName || file.url)

      if (!path) {
        this.$message.error('文件路径异常，无法预览')
        return
      }

      // 2. 调用之前写好的 downloadTemplate 方法
      this.downloadTemplate(path, '预览补交材料')
    }
  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

.order-list-page {
  padding: 40px 0 100px;
}

.page-header {
  margin-bottom: 30px;

  h2 {
    font-weight: 700;
    color: $text-main;
  }

  p {
    color: $text-secondary;
    font-size: 14px;
  }
}

.mb-20 {
  margin-bottom: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;

  i {
    font-size: 60px;
    color: #ddd;
    margin-bottom: 15px;
  }
}

.loading-box {
  text-align: center;
  padding: 40px;
  color: $text-secondary;
}

.audit-remark-alert {
  margin-top: 10px;
  background: #FFF1F0; // 极淡的红色背景
  border: 1px solid #FFA39E;
  padding: 8px 12px;
  border-radius: 12px; // 圆润边角
  display: flex;
  align-items: center;
  gap: 8px;

  i {
    color: #F5222D;
    font-size: 16px;
  }

  .remark-content {
    color: #F5222D;
    font-size: 13px;
    font-weight: 500;
  }
}

.status-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  /* 标签和图标的间距 */
  margin-bottom: 15px;
  /* 与下方按钮的固定间距 */
  justify-content: flex-end;
  /* 向右对齐 */
}

.status-badge {
  /* 基础样式... */
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  /* 强制不换行，保持高度固定 */
}

/* 驳回原因的小图标 */
.remark-icon {
  color: #F5222D;
  /* 警示红 */
  font-size: 16px;
  cursor: help;
  transition: transform 0.3s;

  &:hover {
    transform: scale(1.2);
  }
}

/* 每一个订单卡片的容器 */
.order-item {
  padding: 0 !important;
  /* 方便控制内部边距 */
  display: flex;
  flex-direction: column;

  .order-body {
    display: flex;
    padding: 25px;
    position: relative;
  }
}

.img-box {
  width: 100px;
  height: 75px;
  margin-right: 20px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 12px;
  }
}

.order-info {
  flex: 1;

  .top-row {
    font-size: 12px;
    color: #999;
    margin-bottom: 5px;
  }

  .title {
    font-size: 18px;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 8px;
  }

  .price-row {
    .qty {
      font-size: 13px;
      color: #909399;
      margin-right: 12px;
    }

    .money {
      font-weight: 700;
      color: #ff6b6b;
    }
  }
}

/* 底部工具栏样式 */
.order-footer {
  border-top: 1px solid #f5f7fa;
  padding: 15px 25px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fafbfc; // 底部轻微底色区分
}

.footer-left,
.footer-right {
  flex: 1;
}

.footer-center {
  flex: 2;
  display: flex;
  justify-content: center;
  align-items: center;

  .main-action-btn {
    min-width: 160px;
    height: 44px;
    font-size: 15px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }

  .waiting-text {
    font-size: 14px;
    color: $primary-color;
    font-weight: 500;
  }
}

.footer-right {
  text-align: right;

  .el-button--text {
    color: #909399;
    font-weight: 500;

    &:hover {
      color: $primary-color;
    }
  }
}

/* 状态标签 */
.status-top-right {
  position: absolute;
  top: 25px;
  right: 25px;

  .status-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.status-badge {
  padding: 4px 12px;
  border-radius: 50px;
  font-size: 12px;
  font-weight: 600;

  &.status-0 {
    background: #FFF7E6;
    color: #FA8C16;
  }

  &.status-1 {
    background: #E6F7FF;
    color: #1890FF;
  }

  &.status-2 {
    background: #F9F0FF;
    color: #722ED1;
  }

  &.status-3 {
    background: #FFF1F0;
    color: #F5222D;
  }

  &.status-4 {
    background: #E6FFFB;
    color: #13C2C2;
  }

  &.status-6 {
    background: #F6FFED;
    color: #52C41A;
  }
}

.remark-icon {
  color: #F5222D;
  cursor: help;
  font-size: 16px;
}

/* 预约方案卡片 */
.slot-selection-card {
  margin-top: 20px;
  background: #FFFFFF;
  border: 2px solid #EBF5FF;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(106, 175, 230, 0.06);

  .notice-header {
    color: $primary-color;
    font-weight: 700;
    font-size: 16px;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .notice-tip {
    font-size: 13px;
    color: #909399;
    margin-bottom: 20px;
  }
}

/* 胶囊布局网格 */
.slots-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

/* ★★★ 时间胶囊核心样式 ★★★ */
.time-capsule {
  background-color: #F7F9FC;
  border: 2px solid transparent;
  border-radius: 50px;
  /* 极致圆润 */
  padding: 12px 25px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 150px;

  .date-text {
    font-size: 11px;
    color: #999;
    margin-bottom: 2px;
    text-transform: uppercase;
  }

  .time-text {
    font-size: 16px;
    font-weight: 700;
    color: $text-main;
  }

  &:hover {
    background-color: #EBF5FF;
    border-color: $primary-color;
    transform: translateY(-4px) scale(1.03);
    box-shadow: 0 10px 20px rgba(106, 175, 230, 0.15);

    .time-text {
      color: $primary-color;
    }
  }

  &:active {
    transform: scale(0.96);
  }
}

/* 状态8的正式通知卡片 */
.interview-final-notice {
  margin-top: 20px;
  background: linear-gradient(135deg, #6AAFE6 0%, #4facfe 100%);
  border-radius: 20px;
  padding: 24px;
  color: #fff;
  box-shadow: 0 10px 25px rgba(106, 175, 230, 0.3);

  .final-header {
    font-size: 18px;
    font-weight: 700;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .final-body {
    p {
      margin-bottom: 8px;
      font-size: 15px;
      opacity: 0.95;

      span {
        opacity: 0.7;
      }
    }
  }

  .final-actions {
    margin-top: 20px;
    display: flex;
    gap: 10px;
  }
}

.chat-hint {
  text-align: center;
  font-size: 13px;
  color: #C0C4CC;
  border-top: 1px dashed #eee;
  padding-top: 15px;
}

.feedback-container {
  padding: 10px;

  .feedback-tip {
    font-size: 13px;
    color: #909399;
    margin-bottom: 20px;
  }
}

.person-feedback-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f7fa;

  .p-name {
    font-weight: 600;
    color: $text-main;
  }
}

.upload-slip {
  .label {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 10px;
    color: $text-main;
  }
}

/* 奶蓝色单选按钮定制 */
::v-deep .el-radio-button__inner {
  border-radius: 20px !important;
  margin-left: 5px;
  border: 1px solid #DCDFE6 !important;
}

::v-deep .el-radio-button:first-child .el-radio-button__inner {
  border-left: 1px solid #DCDFE6 !important;
}

.final-result-card {
  margin-top: 15px;
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;

  .result-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 15px;

    .label {
      font-weight: 600;
      color: #2c3e50;
    }
  }

  .shipping-box {
    background: #fff;
    padding: 12px 15px;
    border-radius: 10px;
    border: 1px solid #edf2f7;

    .ship-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #64748b;
      font-size: 14px;

      i {
        font-size: 18px;
        color: #6AAFE6;
      }

      strong {
        color: #2c3e50;
      }
    }
  }
}

.finished-text {
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
}

.quick-preview-box {
  margin-left: 15px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.mini-file-link {
  font-size: 12px !important;
  background: #f0f7ff;
  padding: 2px 8px;
  border-radius: 4px;
  color: $primary-color !important;

  &:hover {
    background: #e6f0ff;
    text-decoration: none;
  }

  i { margin-right: 3px; }
}

.supp-uploader {
  /* 让文件列表里的名字看起来像可以点击的链接 */
  ::v-deep .el-upload-list__item-name {
    color: $primary-color;
    cursor: pointer;
    &:hover { color: $primary-hover; }
  }
}

</style>
