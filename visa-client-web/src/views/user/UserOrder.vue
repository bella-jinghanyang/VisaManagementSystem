<template>
  <div class="order-list-page container-width">
    <!-- 1. 顶部标题与搜索 -->
    <div class="page-header">
      <div class="header-left">
        <h2>我的订单</h2>
        <p>管理您的签证申请进度</p>
      </div>

      <!-- 搜索订单功能 -->
      <div class="header-right">
        <div class="search-wrapper">
          <input v-model="searchKey" placeholder="搜索订单号或签证名称..." @input="handleSearch" />
          <i class="el-icon-search"></i>
        </div>
      </div>
    </div>

    <!-- 2. 分类切换 -->
    <div class="order-tabs">
      <div v-for="tab in tabOptions" :key="tab.key" :class="['tab-item', { active: activeTab === tab.key }]"
        @click="handleTabChange(tab.key)">
        {{ tab.label }}
        <!-- 气泡数字：显示该状态下的订单数量 -->
        <span class="tab-count" v-if="getTabCount(tab.key) > 0">
          {{ getTabCount(tab.key) }}
        </span>
      </div>
    </div>

    <!-- 3. 加载中 -->
    <div v-if="loading" class="loading-box">
      <i class="el-icon-loading"></i> 加载中...
    </div>

    <!-- 4. 订单列表 -->
    <div v-else-if="filteredOrders.length > 0" class="order-list">

      <apple-card v-for="order in filteredOrders" :key="order.id" class="compact-order-card mb-20"
        @click.native="openDetailDrawer(order)">
        <div class="card-inner">
          <!-- 左侧：封面图 -->
          <div class="img-side">
            <img :src="$getImgUrl(parseSnapshot(order.productSnapshot).image)" class="mini-cover">
          </div>

          <!-- 中间：核心信息 -->
          <div class="info-side">
            <div class="order-no-row">单号：{{ order.orderNo }}</div>
            <h3 class="product-title">{{ parseSnapshot(order.productSnapshot).title }}</h3>
            <div class="data-row">
              <span class="qty">{{ order.quantity }}人办理</span>
              <span class="divider"></span>
              <span class="price">￥{{ order.totalAmount }}</span>
            </div>
          </div>

          <!-- 右侧：状态标签与箭头 -->
          <div class="status-side">
            <div :class="['status-text', 'color-' + order.status]">
              {{ statusMap[order.status] }}
            </div>
            <i class="el-icon-arrow-right arrow-icon"></i>
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

    <!-- 侧边沉浸式详情抽屉 -->
    <el-drawer title="订单办理详情" :visible.sync="detailDrawerOpen" direction="rtl" size="450px" custom-class="apple-drawer"
      :append-to-body="true">
      <div class="drawer-content" v-if="selectedOrder.id">
        <!-- 1. 顶部状态横条 -->
        <div class="drawer-header-status" :class="'bg-status-' + selectedOrder.status">
          <div class="status-main">{{ statusMap[selectedOrder.status] }}</div>
          <div class="status-desc">单号：{{ selectedOrder.orderNo }}</div>
        </div>

        <div class="drawer-body-scroll">
          <!-- 2. 产品信息概览卡 -->
          <div class="inner-card product-summary">
            <img :src="$getImgUrl(parseSnapshot(selectedOrder.productSnapshot).image)" class="summary-img">
            <div class="summary-text">
              <h4>{{ parseSnapshot(selectedOrder.productSnapshot).title }}</h4>
              <p>办理人数：{{ selectedOrder.quantity }}人 | 总额：￥{{ selectedOrder.totalAmount }}</p>
            </div>
          </div>

          <!-- 3. 驳回原因提醒 (仅状态3显示) -->
          <div v-if="selectedOrder.status === 3" class="inner-card error-card">
            <div class="card-label"><i class="el-icon-warning"></i> 审核未通过</div>
            <p class="error-msg">您的申请材料被退回，请查阅下方各申请人的修改建议并重新上传。</p>
          </div>

          <!-- 4. 办理进度明细区 (状态4及以上显示) -->
          <div v-if="selectedOrder.status >= 4  && selectedOrder.visaResult" class="inner-card result-card">
            <div class="card-label">签证办理结果</div>
            <div v-for="(res, idx) in parseJson(selectedOrder.visaResult)" :key="idx" class="drawer-res-item">
              <span class="p-name" style="font-weight: bold; color: #1d1d1f;">
                {{ (selectedOrder.applicantList && selectedOrder.applicantList[idx]) ?
            selectedOrder.applicantList[idx].name : '申请人 ' + (idx + 1) }}:
              </span>
              <el-tag :type="getResultTagType(res.status)" size="mini" round>{{ getResultText(res.status) }}</el-tag>
              <!-- ★★★ 核心新增：如果这个人状态是 Check (3)，显示补件区 ★★★ -->
              <div v-if="res.status == 3" class="check-box"
                style="margin-top: 10px; padding: 12px; background: #FFFBE6; border-radius: 10px; border: 1px solid #FFE58F;">
                <p style="font-size: 12px; color: #856404; margin-bottom: 8px;">
                  <i class="el-icon-warning"></i> 需补交电子材料，请查看指引：
                </p>

                <div style="display: flex; gap: 10px;">
                  <!-- 下载管理员发的 Check 通知单 -->
                  <el-button v-if="res.fileUrl" type="warning" size="mini" plain round icon="el-icon-download"
                    @click="downloadTemplate(res.fileUrl)">
                    查看补件指引
                  </el-button>

                  <!-- 开启补件上传弹窗 -->
                  <el-button type="primary" size="mini" round icon="el-icon-upload"
                    @click="handleUploadSupplementary(selectedOrder, idx)">
                    上传补充材料
                  </el-button>
                </div>

                <!-- 显示已上传的文件数 -->
                <div v-if="getSuppFileList(selectedOrder, idx).length > 0"
                  style="margin-top: 8px; font-size: 11px; color: #67C23A;">
                  已成功补传 {{ getSuppFileList(selectedOrder, idx).length }} 份文件
                </div>
              </div>

              <div class="p-memo" v-if="res.memo">{{ res.memo }}</div>
            </div>

            <!-- 物流信息 -->
            <div class="drawer-shipping" v-if="selectedOrder.trackingNumber">
              <i class="el-icon-truck"></i> 物流单号：{{ selectedOrder.trackingNumber }}
            </div>
          </div>

          <!-- 5. 面试预约决策区 (状态7显示) -->
          <div v-if="selectedOrder.status === 7 && selectedOrder.interviewSlots && !selectedOrder.interviewTime"
            class="inner-card slot-card">
            <div class="card-label">选择面试时间</div>
            <div class="drawer-slots">
              <div v-for="time in parseJson(selectedOrder.interviewSlots)" :key="time" class="drawer-time-capsule"
                @click="handleConfirmTime(selectedOrder, time)">
                {{ time }}
              </div>
            </div>

            <div style="text-align: center; margin-top: 15px;">
              <el-button type="text" style="color: #909399; font-size: 13px;" @click="openRejectSlotsDialog">
                时间都不方便？点击反馈要求
              </el-button>
            </div>
          </div>

          <!-- 场景 B：状态 7 且 已选时间，等待凭证 -->
            <div v-if="selectedOrder.status === 7 && selectedOrder.interviewTime" class="inner-card result-card"
              style="border: 1px solid #BF5AF2;">
              <div class="card-label" style="color: #BF5AF2;"><i class="el-icon-loading"></i> 预约确认中</div>
              <p style="font-size: 14px;">您已选择：<strong>{{ selectedOrder.interviewTime }}</strong></p>
              <p style="font-size: 12px; color: #999; margin-top: 10px;">
                我们的专业顾问正在使馆系统为您锁定名额，稍后将为您下发正式预约单。
              </p>
            </div>

            <!-- 场景 C：状态 8（管理员传完 PDF 后） -->
            <div v-if="selectedOrder.status === 8" class="inner-card interview-info-card">
              <div class="card-label">面试预约成功</div>
              <p><strong>面试时间：</strong>{{ selectedOrder.interviewTime }}</p>
              <el-button type="primary" size="mini" @click="downloadTemplate(selectedOrder.interviewFile)">
                下载官方预约确认单
              </el-button>
            </div>

          <!-- 驳回留言弹窗 -->
          <el-dialog title="反馈面试时间要求" :visible.sync="rejectSlotOpen" width="400px" append-to-body>
            <p style="font-size: 13px; color: #909399; margin-bottom: 10px;">请告知您方便的时间范围（例如：希望在下周五下午）：</p>
            <el-input type="textarea" v-model="slotRejectRemark" :rows="3" placeholder="请输入您的要求..."></el-input>
            <div slot="footer">
              <el-button @click="rejectSlotOpen = false" size="small">取消</el-button>
              <el-button type="primary" @click="submitSlotReject" size="small">提交反馈</el-button>
            </div>
          </el-dialog>

          <!-- 6. 面试正式通知区 (状态8显示) -->
          <div v-if="selectedOrder.status === 8" class="inner-card interview-info-card">
            <div class="card-label">面试通知</div>
            <p><strong>时间：</strong>{{ selectedOrder.interviewTime }}</p>
            <p><strong>地点：</strong>{{ selectedOrder.interviewLocation || '请见预约单' }}</p>
            <el-button v-if="selectedOrder.interviewFile" type="primary" size="mini" plain icon="el-icon-download"
              @click="downloadTemplate(selectedOrder.interviewFile)">下载预约单</el-button>
          </div>

          <!-- 7. 评价与追评展示区 -->
          <div v-if="selectedOrder.isCommented == 1" class="inner-card comment-card">
            <div class="card-label">我的评价</div>
            <el-rate v-model="selectedOrder.commentRate" disabled size="mini"></el-rate>
            <p class="comment-text">“{{ selectedOrder.commentText }}”</p>
            <div v-if="selectedOrder.additionalContent" class="drawer-add-comment">
              <span class="label">追评：</span>{{ selectedOrder.additionalContent }}
            </div>
          </div>

          <!-- 状态 9 且 用户还没填单号时显示 -->
          <!-- 状态 9（待寄送）或 状态 10（已寄出）均显示此卡片 -->
          <div v-if="[9, 10].includes(selectedOrder.status)" class="inner-card shipping-guide-card">
            <div class="card-label">
              <i class="el-icon-truck"></i>
              {{ selectedOrder.status === 9 ? '邮寄原件指引' : '邮寄进度确认' }}
            </div>

            <!-- 情况 A：处于状态 9 - 引导用户寄送 -->
            <template v-if="selectedOrder.status === 9">
              <div class="agency-address"
                style="background: rgba(106, 175, 230, 0.05); padding: 15px; border-radius: 12px; margin-bottom: 20px;">
                <p style="font-size: 13px; color: #909399; margin-bottom: 10px;">电子材料初审已通过，请将护照原件寄往：</p>
                <div class="address-item" style="margin-bottom:5px"><strong>收货人：</strong>{{ agencyInfo.name }}</div>
                <div class="address-item" style="margin-bottom:5px"><strong>联系电话：</strong>{{ agencyInfo.phone }}</div>
                <div class="address-item" style="margin-bottom:10px"><strong>寄送地址：</strong>{{ agencyInfo.address }}
                </div>
                <el-button type="text" size="mini" icon="el-icon-document-copy"
                  @click="copyAgencyAddress">复制完整地址</el-button>
              </div>

              <el-divider></el-divider>

              <div class="user-express-box">
                <p class="input-tip" style="font-size: 13px; color: #606266; margin-bottom: 12px;">寄出后请在此填写快递单号：</p>
                <el-select v-model="mailForm.company" placeholder="选择快递公司" size="small"
                  style="width: 100%; margin-bottom: 10px;">
                  <el-option label="顺丰速运" value="顺丰" />
                  <el-option label="EMS" value="EMS" />
                  <el-option label="中通快递" value="中通" />
                  <el-option label="圆通速递" value="圆通" />
                  <el-option label="韵达快递" value="韵达" />
                </el-select>
                <el-input v-model="mailForm.no" placeholder="请输入快递单号" size="small">
                  <el-button slot="append" type="primary" @click="submitUserExpress" :loading="loading">确认提交</el-button>
                </el-input>
              </div>
            </template>

            <!-- 情况 B：处于状态 10 - 显示已寄出状态 -->
            <template v-else-if="selectedOrder.status === 10">
              <div class="sent-status-box" style="text-align: center; padding: 20px 10px;">
                <i class="el-icon-circle-check" style="color: #67C23A; font-size: 48px;"></i>
                <p style="margin-top: 15px; font-size: 16px; font-weight: bold; color: #67C23A;">原件已寄出，中介待签收</p>
                <div
                  style="margin-top: 15px; background: #f8fafc; padding: 12px; border-radius: 8px; text-align: left;">
                  <p style="font-size: 13px; color: #606266; margin: 0;">
                    <strong>快递信息：</strong>{{ selectedOrder.expressToAgency || mailForm.no }}
                  </p>
                  <p style="font-size: 12px; color: #999; margin-top: 5px;">
                    中介签收后，订单将自动进入使馆办理环节。
                  </p>
                </div>
              </div>
            </template>
          </div>

        </div>

        <!-- 8. 底部吸底主动作按钮 -->
        <div class="drawer-footer">
          <!-- 状态 0: 去支付 -->
          <el-button v-if="selectedOrder.status === 0" type="primary" class="drawer-main-btn"
            @click="handlePay(selectedOrder)">立即支付</el-button>

          <!-- 状态 1/3: 去传资料 -->
          <el-button v-if="[1, 3].includes(selectedOrder.status)" type="primary" class="drawer-main-btn"
            @click="handleUpload(selectedOrder)">
            {{ selectedOrder.status === 1 ? '上传申请材料' : '修改申请材料' }}
          </el-button>

          <!-- 状态 8: 反馈面试结果 -->
          <el-button v-if="selectedOrder.status === 8" type="success" class="drawer-main-btn"
            @click="handleInterviewFinish(selectedOrder)">我已面试完</el-button>

          <!-- 状态 5: 确认收货 -->
          <!-- 找到抽屉内部逻辑，在状态4展示区下方，或者作为独立卡片 -->
          <div v-if="selectedOrder.status === 5" class="inner-card result-card" style="border: 2px solid #34C759;">
            <div class="card-label" style="color: #34C759;"><i class="el-icon-success"></i> 结果已出，正在返还</div>

            <!-- 1. 获签凭证下载 (非常重要) -->
            <div class="result-files-box"
              style="background: #F0F9EB; padding: 12px; border-radius: 12px; margin-bottom: 15px;">
              <p style="font-size: 13px; font-weight: bold; color: #67C23A; margin-bottom: 10px;">请查收电子获签凭证：</p>
              <div v-for="(res, idx) in parseJson(selectedOrder.visaResult)" :key="idx" class="file-item"
                style="margin-bottom: 5px;">
                <span style="font-size: 13px; color: #333; min-width: 80px;">
                  {{ (selectedOrder.applicantList && selectedOrder.applicantList[idx]) ?
                    selectedOrder.applicantList[idx].name : '申请人 ' + (idx + 1) }}：
                </span>

                <el-button v-if="res.fileUrl" type="text" size="mini" icon="el-icon-download"
                  @click="downloadTemplate(res.fileUrl)">
                  点击预览/下载凭证
                </el-button>
                <span v-else style="font-size: 12px; color: #999;">(未上传电子版)</span>
              </div>
            </div>

            <!-- 2. 寄回物流单号 -->
            <div class="shipping-info-box" style="background: #f8fafc; padding: 12px; border-radius: 12px;">
              <p style="font-size: 13px; font-weight: bold; color: #1d1d1f; margin-bottom: 8px;">
                <i class="el-icon-truck"></i> 寄回物流信息
              </p>
              <div v-if="selectedOrder.trackingNumber">
                <div style="font-size: 14px; color: #333; margin-bottom: 5px;">
                  快递单号：<span style="color: #409EFF; font-weight: bold;">{{ selectedOrder.trackingNumber }}</span>
                </div>
                <p style="font-size: 11px; color: #999;">请留意顺丰/EMS推送，护照原件即将送达。</p>
              </div>
              <div v-else style="color: #FA8C16; font-size: 13px;">
                管理员暂未录入物流单号，请稍后查看。
              </div>
            </div>

            <el-button v-if="selectedOrder.status === 5" type="primary" class="drawer-main-btn"
              @click="handleConfirmReceipt(selectedOrder)">确认收到护照</el-button>

          </div>

          <!-- 状态 6: 评价/追评 -->
          <el-button v-if="selectedOrder.status === 6 && selectedOrder.isCommented != 1" type="primary"
            class="drawer-main-btn" @click="handleOpenComment(selectedOrder)">评价服务</el-button>
          <el-button
            v-if="selectedOrder.status === 6 && selectedOrder.isCommented == 1 && !selectedOrder.additionalContent"
            type="primary" plain class="drawer-main-btn" @click="handleOpenFollowUp(selectedOrder)">追加评价</el-button>

          <div class="contact-support" @click="handleOpenChat(selectedOrder)">
            <i class="el-icon-chat-dot-round"></i> 联系签证顾问
          </div>
        </div>
      </div>
    </el-drawer>

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
          :file-list="tempFileList" :on-remove="handleSuppRemove" :on-change="handleFileChange"
          :on-preview="handleSuppPreview" multiple list-type="text">
          <el-button size="small" type="primary" icon="el-icon-upload">点击选择文件</el-button>
        </el-upload>
      </div>

      <div slot="footer">
        <el-button @click="suppOpen = false" round>取消</el-button>
        <el-button type="primary" @click="submitSupplementary" round :loading="loading">确认提交</el-button>
      </div>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog title="评价您的签证服务" :visible.sync="commentOpen" width="450px" append-to-body custom-class="apple-dialog">
      <div style="text-align:center; padding: 10px 0;">
        <p style="margin-bottom: 15px; color: #606266;">您对本次服务的满意度</p>
        <!-- ★ 修正：从 0 开始，不默认给五星 -->
        <el-rate v-model="commentForm.rating" :texts="['极差', '一般', '不错', '很棒', '完美']" show-text></el-rate>
      </div>

      <el-input type="textarea" v-model="commentForm.content" placeholder="写下您的办签心得..." :rows="3"
        style="margin-top:20px" />

      <!-- 晒图上传区域 -->
      <div class="comment-upload-title">上传办签照片/签证页 (最多3张)</div>
      <el-upload action="/api/common/upload" :headers="headers" list-type="picture-card" :limit="3"
        :on-success="handleCommentImageSuccess" :on-remove="handleCommentImageRemove" :file-list="commentFileList">
        <i class="el-icon-camera"></i>
      </el-upload>

      <div slot="footer" class="dialog-footer">
        <el-button @click="commentOpen = false" round>取 消</el-button>
        <el-button type="primary" @click="submitMyComment" round :loading="loading">发布评价</el-button>
      </div>
    </el-dialog>

    <!-- 追加评价弹窗 -->
    <el-dialog title="追加评价" :visible.sync="followUpOpen" width="450px" append-to-body custom-class="apple-dialog">
      <p style="color: #909399; font-size: 13px; margin-bottom: 15px;">
        您可以补充更多关于办签细节的感受...
      </p>
      <el-input type="textarea" v-model="followUpForm.content" placeholder="请输入您的追评内容..." :rows="4" />
      <div slot="footer">
        <el-button @click="followUpOpen = false" round>取消</el-button>
        <el-button type="primary" @click="submitFollowUp" round :loading="loading">提交追评</el-button>
      </div>
    </el-dialog>
  </div>

</template>

<script>
/* eslint-disable */
import { addAdditionalComment, addComment } from '@/api/comment';
import AppleCard from '@/components/AppleCard';
import request from '@/utils/request';
import { listMyOrders, getStripePayUrl } from '@/api/order';

export default {
  name: 'UserOrder',
  components: { AppleCard },
  data() {
    return {
      // --- 核心列表数据 ---
      loading: false,
      orderList: [],

      // --- 抽屉与详情控制 ---
      detailDrawerOpen: false, // 控制侧边详情抽屉
      selectedOrder: {}, // 当前抽屉中展示的订单（深度克隆）

      currentOrder: {},
      // --- 状态映射字典 ---
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
        8: '待面试',
        9: '待寄送原件',
        10: '原件已寄出'
      },

      // --- 面试与反馈相关 ---
      feedbackOpen: false,
      feedbackList: [], // 存储多人的结果
      slipUrl: '', // 存凭证图
      rejectSlotOpen: false,
      slotRejectRemark: '',

      // --- 补充材料(Check)相关 ---
      tempFileList: [], // 临时存储本次上传的文件
      suppOpen: false, // 控制补件弹窗
      targetPersonIdx: 0, // 正在为第几个人补件

      // --- 评价相关 ---
      commentOpen: false, // 控制评价弹窗
      commentFileList: [], // 用于存储 el-upload 展示的文件列表
      commentForm: {
        rating: 5,
        content: '',
        images: '',
        orderId: null,
        productId: null,
        customerId: null
      },
      followUpOpen: false, // 控制追评弹窗
      followUpForm: {
        id: null, // 评价记录的ID
        content: ''
      },

      // --- 寄送 ---
      mailForm: {
        company: 'SF', // 默认顺丰
        no: '',
        orderId: null,
        orderNo: ''
      },
      // 中介收货地址（实际开发中可以从后端配置接口获取，毕设可以直接写死）
      agencyInfo: {
        name: '全球通签证中心-资料组',
        phone: '010-88888888',
        address: '北京市朝阳区世贸天阶A座1205室'
      },

      // --- 筛选与搜索 ---
      activeTab: 'all',
      searchKey: '',
      tabOptions: [
        { label: '全部', key: 'all' },
        { label: '待付款', key: 'unpaid' },
        { label: '待办材料', key: 'action' },
        { label: '寄送材料', key: 'mailing' },
        { label: '办理中', key: 'processing' },
        { label: '待收货', key: 'shipping' },
        { label: '待评价', key: 'review' }
      ],

      // --- 基础配置 ---
      headers: { Authorization: 'Bearer ' + localStorage.getItem('Client-Token') }

    }
  },
  created() {
    this.getList()
    this.checkStripeResult();
  },
  beforeDestroy() {
    this.$bus.$off('order-updated')
  },
  computed: {
    /** 多重过滤（分类 + 搜索） */
    filteredOrders() {
      return this.orderList.filter(order => {
        // A. 分类过滤
        let tabMatch = true
        const s = order.status
        const isComm = order.isCommented
        if (this.activeTab === 'unpaid') tabMatch = s === 0
        else if (this.activeTab === 'action') tabMatch = [1, 3].includes(s)
        else if (this.activeTab === 'mailing') tabMatch = [9, 10].includes(s)
        else if (this.activeTab === 'processing') tabMatch = [2, 4, 7, 8].includes(s)
        else if (this.activeTab === 'shipping') tabMatch = s === 5
        else if (this.activeTab === 'review') {
          tabMatch = (s === 6 && isComm !== '1')
        }

        // B. 搜索过滤
        const snapshot = this.parseSnapshot(order.productSnapshot)
        const searchMatch = order.orderNo.includes(this.searchKey) ||
          snapshot.title.toLowerCase().includes(this.searchKey.toLowerCase())

        return tabMatch && searchMatch
      })
    }
  },
  methods: {
    /** 获取列表数据 */
    getList() {
      this.loading = true
      const userStr = localStorage.getItem('Client-User')
      if (!userStr) return
      const user = JSON.parse(userStr)

      listMyOrders(user.id).then(res => {
        this.orderList = res.data || []
        // 如果当前抽屉开着，同步更新抽屉里的数据
        if (this.detailDrawerOpen && this.selectedOrder.id) {
          const latest = this.orderList.find(o => o.id === this.selectedOrder.id)
          if (latest) this.selectedOrder = JSON.parse(JSON.stringify(latest))
        }
      }).finally(() => {
        this.loading = false
      })
    },
    /** 核心：开启详情抽屉 */
    openDetailDrawer(order) {
      this.selectedOrder = JSON.parse(JSON.stringify(order))
      // ★ 每次打开抽屉，把输入框清空，或者回显已有的单号
      this.myExpressNo = order.expressToAgency || ''
      this.detailDrawerOpen = true
    },

    // 解析快照 JSON
    parseSnapshot(jsonStr) {
      if (!jsonStr) return {}
      try {
        return JSON.parse(jsonStr)
      } catch (e) {
        return {}
      }
    },


    /** 
     * 发起 Stripe 支付 
     */

    handlePay(order) {
      const loading = this.$loading({
        lock: true,
        text: '正在前往安全支付中心...',
        spinner: 'el-icon-loading',
        background: 'rgba(255, 255, 255, 0.7)'
      })

      // 调用 Stripe 接口
      getStripePayUrl(order.orderNo).then(res => {
        loading.close()
        if (res.code === 200) {
          // ★★★ 核心：这里一定要用 res.data ！！ ★★★
          // 如果写成 res.msg，就会跳到那个“下单成功”的错误页面
          console.log("即将跳转到 Stripe:", res.data);
          window.location.href = res.data;
        } else {
          this.$message.error(res.msg)
        }
      }).catch(err => {
        loading.close()
        this.$message.error("支付网关连接失败")
      })
    },



    checkStripeResult() {
      // 1. 这种方式能抓取到无论是 # 之前还是之后的参数
      const fullUrl = window.location.href;
      console.log(">>> 调试：当前浏览器地址栏全路径:", fullUrl);

      const urlObj = new URL(fullUrl.replace('#/', '')); // 处理 hash 模式干扰
      const payResult = urlObj.searchParams.get('payResult');
      const orderNo = urlObj.searchParams.get('orderNo');

      console.log(">>> 调试：尝试提取到的参数:", { payResult, orderNo });

      if (payResult === 'success' && orderNo) {
        console.log(">>> 命中支付成功逻辑，准备请求后端 confirm...");
        this.$router.replace('/user/orders');

        request({
          url: '/client/stripe/confirm',
          method: 'post',
          data: { orderNo: orderNo }
        }).then(res => {
          console.log(">>> 后端 confirm 接口返回:", res);
          if (res.code === 200) {
            this.$message.success('支付同步成功，订单已进入下一阶段');
            this.getList(); // 刷新列表
          }
        }).catch(err => {
          console.error(">>> 请求 confirm 接口异常:", err);
        });
      }
    },
    // 跳转上传页 (下一步要做这个页面)
    handleUpload(order) {
      this.$router.push(`/order/upload?orderId=${order.id}`)
    },

    // 跳转详情
    goDetail(id) {
      this.$router.push(`/order/detail/${id}`) // 还没做，先预留
    },

    // 判断是否是 JSON 数组字符串
    isJsonArray(str) {
      if (!str) return false
      return typeof str === 'string' && str.startsWith('[')
    },

    /** 1. 解析 JSON 字符串 */
    parseJson(str) {
      if (!str) return []
      try {
        return JSON.parse(str)
      } catch (e) {
        return []
      }
    },

    /** 2. 用户点击胶囊确认时间 */
    handleConfirmTime(order, selectedTime) {
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

    openRejectSlotsDialog() {
      this.slotRejectRemark = '';
      this.rejectSlotOpen = true;
    },
    submitSlotReject() {
      if (!this.slotRejectRemark) return this.$message.warning('请输入您的要求');

      const data = {
        id: this.selectedOrder.id,
        userRemark: "【时间不便】" + this.slotRejectRemark, 
        // 逻辑：一旦用户驳回，我们可以清空之前的 slots，触发管理员重新发送
        interviewSlots: null
      };

      request({
        url: '/client/order/update',
        method: 'put',
        data: data
      }).then(res => {
        this.$message.success('反馈已发送，顾问将重新为您安排时间');
        this.rejectSlotOpen = false;
        this.getList();
      });
    },

    /** 3. 下载面试确认单 */
    downloadFile(url) {
      if (!url) return
      window.open('http://localhost:8080' + url, '_blank')
    },

    /** 4. 用户反馈面试完成 */
    /** 1. 点击“我已面试完”按钮触发 */
    handleInterviewFinish(order) {
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
    handleSlipSuccess(res) {
      this.slipUrl = res.fileName
      this.$message.success('凭证上传成功')
    },

    /** 3. 提交反馈到后端 */
    submitFeedback() {
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
    downloadTemplate(url, name) {
      if (!url) return
      // 直接新窗口打开
      window.open('http://localhost:8080' + url, '_blank')
    },

    handleConfirmReceipt(order) {
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

    /** 打开留言板 */
    handleOpenChat(order) {
      // 跳转到 AI 助手或者留言页 (根据你之前的逻辑)
      this.$router.push(`/ai-chat?orderId=${order.id}`)
    },

    /** 1. 核心：监听文件列表变化 */
    handleFileChange(file, fileList) {
      // 只要文件列表变了（添加、上传成功、删除），就同步给 tempFileList
      this.tempFileList = fileList
    },

    openSuppDialog(order, index) {
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
    getSuppFileList(order, idx) {
      if (!order.supplementaryMaterials) return []
      try {
        const allData = JSON.parse(order.supplementaryMaterials)
        return allData[idx.toString()] || [] // 根据索引拿数组
      } catch (e) { return [] }
    },

    /** 打开补件弹窗并实现“回显” */
    handleUploadSupplementary(order, idx) {
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
    handleSuppSuccess(res, file, fileList) {
      if (res.code === 200) {
        file.fileName = res.fileName // 存入后端返回的路径
        this.tempFileList = fileList
        this.$message.success('上传成功')
      }
    },

    /** 处理补件删除 */
    handleSuppRemove(file, fileList) {
      this.tempFileList = fileList
    },

    /** 提交补充材料（保存到数据库） */
    submitSupplementary() {
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
    getResultTagType(status) {
      if (status === 1) return 'success' // 绿色
      if (status === 3) return 'warning' // 黄色
      return 'danger' // 红色
    },
    getResultText(status) {
      if (status === 1) return '已出签'
      if (status === 3) return '审查中(Check)'
      return '已拒签'
    },
    /** 处理补件列表中的点击预览 */
    handleSuppPreview(file) {
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
    },

    /** 评价图片上传成功 */
    handleCommentImageSuccess(res, file, fileList) {
      if (res.code === 200) {
        this.commentFileList = fileList
        this.$message.success('图片上传成功')
      }
    },

    /** 评价图片移除 */
    handleCommentImageRemove(file, fileList) {
      this.commentFileList = fileList
    },

    /** 打开评价弹窗方法 */
    handleOpenComment(order) {
      this.currentOrder = order
      this.commentFileList = [] // 重置图片列表
      this.commentForm = {
        rating: 0, // 初始 0 星
        content: '',
        images: '',
        orderId: order.id,
        productId: order.productId,
        customerId: JSON.parse(localStorage.getItem('Client-User')).id
      }
      this.commentOpen = true
    },

    /** 提交方法 */
    submitMyComment() {
      if (this.commentForm.rating === 0) {
        return this.$message.warning('请给本次服务打个分吧')
      }
      if (!this.commentForm.content.trim()) {
        return this.$message.warning('请写一点评价内容')
      }

      this.loading = true
      addComment(this.commentForm).then(res => {
        this.$message.success('评价发布成功！')
        this.commentOpen = false

        // 重新拉取订单列表
        // 这样 orderList 里的 isCommented 就会变成最新的 '1'，角标自然就减 1 了
        this.getList()

        // \通知 Navbar 刷新待办总数
        // 既然评价完了，这单就不算“待办”了，导航栏数字也要减 1
        this.$bus.$emit('order-updated')
      }).finally(() => {
        this.loading = false
      })
    },

    handleTabChange(key) {
      this.activeTab = key
    },
    handleSearch() {
      // computed 会自动响应 searchKey 的变化
    },
    /** 获取每个 Tab 的订单数量统计 */
    getTabCount(key) {
      // 1. 全部订单不显示角标
      if (key === 'all') return 0

      // 2. 过滤逻辑
      return this.orderList.filter(order => {
        const s = order.status
        const isComm = order.isCommented

        // 待付款：状态 0
        if (key === 'unpaid') return s === 0

        // 待办材料（核心任务区）：支付完没传(1) 或 审核没过需补交(3) 
        if (key === 'action') return [1, 3].includes(s)
        if (key === 'mailing') return [9, 10].includes(s)

        // 办理中：正在审核(2)、等待录入方案(7)、等待面试(8)、使馆办理中(4)
        if (key === 'processing') return [2, 4, 7, 8].includes(s)

        // 待收货：管理员已发货(5)
        if (key === 'shipping') return s === 5

        // 待评价：已结案(6) 且 评价标志位不为 1
        if (key === 'review') return (s === 6 && isComm !== '1')

        return false
      }).length
    },
    /** 打开追评弹窗 */
    handleOpenFollowUp(order) {
      this.followUpForm = {
        // 使用联表查出来的 commentId
        id: order.commentId,
        content: ''
      }
      this.followUpOpen = true
    },

    /** 提交追评 */
    submitFollowUp() {
      if (!this.followUpForm.content.trim()) {
        return this.$message.warning('请输入追评内容')
      }
      this.loading = true

      const data = {
        id: this.followUpForm.id, // 这个 ID 就是订单里带的 commentId
        additionalContent: this.followUpForm.content
      }

      addAdditionalComment(data).then(res => {
        this.$message.success('追加评价成功')
        this.followUpOpen = false

        // 重新获取列表，让新内容显示在气泡里
        this.getList()
      }).finally(() => {
        this.loading = false
      })
    },
    /** 提交用户寄给中介的单号 */
    /** 提交用户寄给中介的单号 */
    submitUserExpress() {
      if (!this.mailForm.no) return this.$message.error('请输入快递单号');
      const userAddr = JSON.parse(this.selectedOrder.mailingAddress || '{}');

      this.loading = true;

      const logisticsData = {
        orderId: this.selectedOrder.id,
        orderNo: this.selectedOrder.orderNo,
        courierCompany: this.mailForm.company,
        trackingNo: this.mailForm.no,
        senderName: userAddr.contactName || '客户',
        senderPhone: userAddr.contactPhone || '',
        mailAddress: `收件人：${this.agencyInfo.name}, 地址：${this.agencyInfo.address}`,
      };

      // ★★★ 修正路径：必须与 Controller 的 RequestMapping 对应 ★★★
      request({
        url: '/client/order/submitMailing', // 注意：是 client/order/submitMailing
        method: 'post',
        data: logisticsData
      }).then(res => {
        if (res.code === 200) {
          this.$message.success('单号提交成功！');
          this.selectedOrder.status = 10; // 前端本地同步状态
          this.detailDrawerOpen = false;
          this.getList();
        }
      }).finally(() => {
        this.loading = false;
      });
    },

    /** 辅助：复制地址 */
    copyAgencyAddress() {
      const text = `收件人：${this.agencyInfo.name}\n电话：${this.agencyInfo.phone}\n地址：${this.agencyInfo.address}`;
      navigator.clipboard.writeText(text).then(() => {
        this.$message.success('地址已复制到剪贴板');
      });
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
  padding-top: 80px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;

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

  i {
    margin-right: 3px;
  }
}

.supp-uploader {

  /* 让文件列表里的名字看起来像可以点击的链接 */
  ::v-deep .el-upload-list__item-name {
    color: $primary-color;
    cursor: pointer;

    &:hover {
      color: $primary-hover;
    }
  }
}

.comment-upload-title {
  font-size: 13px;
  color: #909399;
  margin: 20px 0 10px;
  text-align: left;
}

/* 让上传框小一点，符合 Apple 的精致感 */
::v-deep .el-upload--picture-card {
  width: 80px;
  height: 80px;
  line-height: 90px;
  border-radius: 12px;
  background-color: #F5F7FA;
  border: 1px dashed #DCDFE6;
}

::v-deep .el-upload-list--picture-card .el-upload-list__item {
  width: 80px;
  height: 80px;
  border-radius: 12px;
}

/* 搜索框样式 */
.search-wrapper {
  position: relative;
  width: 280px;

  input {
    width: 100%;
    height: 40px;
    background: #fff;
    border: 1px solid #EBF5FF;
    border-radius: 12px;
    padding: 0 40px 0 15px;
    font-size: 14px;
    transition: all 0.3s;

    &:focus {
      outline: none;
      border-color: $primary-color;
      box-shadow: 0 0 0 4px rgba(106, 175, 230, 0.1);
    }
  }

  i {
    position: absolute;
    right: 15px;
    top: 12px;
    color: #999;
  }
}

/* Tab 切换样式 */
.order-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.5);
  padding: 8px;
  border-radius: 16px;

  .tab-item {
    padding: 10px 20px;
    border-radius: 12px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 600;
    color: #86868b;
    transition: all 0.3s;
    position: relative;

    &:hover {
      color: $primary-color;
    }

    &.active {
      background: #fff;
      color: $primary-color;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    }

    .tab-count {
      position: absolute;
      top: -2px;
      right: 2px;
      background: #FF6B6B;
      color: #fff;
      font-size: 10px;
      padding: 2px 6px;
      border-radius: 10px;
      transform: scale(0.8) translate(50%, -50%);
    }
  }
}

.finished-section {
  margin-top: 15px;
}

.comment-bubble {
  background-color: #F7F9FC;
  border: 1px solid #EDF2F7;
  border-radius: 16px;
  padding: 15px;
  margin-top: 10px;

  .bubble-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .bubble-tag {
      font-size: 11px;
      color: $primary-color;
      font-weight: 700;
    }
  }

  .comment-text {
    font-size: 14px;
    color: #4A5568;
    line-height: 1.5;
    font-style: italic;
    margin: 0;
  }

  .admin-reply-box {
    margin-top: 10px;
    padding: 10px;
    background: #fff;
    border-radius: 8px;
    font-size: 13px;
    color: #718096;

    .reply-label {
      font-weight: 700;
      color: $primary-color;
    }
  }

  .additional-box {
    margin-top: 12px;
    padding-top: 10px;
    border-top: 1px dashed #CBD5E0;

    .add-header {
      font-size: 12px;
      color: #A0AEC0;
      margin-bottom: 4px;
    }

    .add-content {
      font-size: 14px;
      color: #2D3748;
      font-weight: 600;
      margin: 0;
    }
  }

  .bubble-footer {
    text-align: right;
    margin-top: 5px;

    .el-button--text {
      padding: 0;
      color: $primary-color;
      font-size: 12px;
    }
  }
}

/* 1. 主列表卡片精简样式 */
.compact-order-card {
  padding: 15px 20px !important;
  cursor: pointer;

  .card-inner {
    display: flex;
    align-items: center;
  }

  .img-side {
    margin-right: 15px;

    .mini-cover {
      width: 60px;
      height: 45px;
      border-radius: 8px;
      object-fit: cover;
    }
  }

  .info-side {
    flex: 1;

    .order-no-row {
      font-size: 11px;
      color: #bbb;
      margin-bottom: 4px;
    }

    .product-title {
      font-size: 15px;
      font-weight: 700;
      color: $text-main;
      margin-bottom: 5px;
    }

    .data-row {
      font-size: 12px;
      color: #86868b;

      .price {
        color: #ff6b6b;
        font-weight: bold;
      }

      .divider {
        margin: 0 8px;
      }
    }
  }

  .status-side {
    text-align: right;
    display: flex;
    align-items: center;
    gap: 10px;

    .status-text {
      font-size: 13px;
      font-weight: 600;
    }

    .arrow-icon {
      color: #DCDFE6;
      font-size: 18px;
    }
  }
}

/* 2. 抽屉样式重写 */
::v-deep .apple-drawer {
  background-color: #F5F5F7 !important;
  /* Apple背景色 */
  border-radius: 30px 0 0 30px;
  /* 左侧大圆角 */
  outline: none;

  .el-drawer__header {
    margin-bottom: 0;
    padding: 20px;
    font-weight: 700;
  }
}

.drawer-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.drawer-header-status {
  padding: 30px 20px;
  color: #fff;

  /* 0: 待支付 - 亮橙色 (提醒行动) */
  &.bg-status-0 {
    background: linear-gradient(135deg, #FF9500, #FFCC00);
  }

  /* 1: 待上传材料 / 3: 需补交材料 - 经典蓝 (用户操作区) */
  &.bg-status-1,
  &.bg-status-3 {
    background: linear-gradient(135deg, #007AFF, #5AC8FA);
  }

  /* 2: 材料审核中 - 优雅紫 (中介正在内部审理) */
  &.bg-status-2 {
    background: linear-gradient(135deg, #AF52DE, #D6A4EE);
  }

  /* 9: 待寄送原件 - 中性灰 (动作停滞，等待物理交互) */
  &.bg-status-9 {
    background: linear-gradient(135deg, #8E8E93, #AEAEB2);
  }

  /* 10: 原件已寄出 - 琥珀金 (物流动态) */
  &.bg-status-10 {
    background: linear-gradient(135deg, #E6A23C, #F3AF4B);
  }

  /* 7: 材料过审，待预约面试 - 玫紫色 (关键转折点) */
  &.bg-status-7 {
    background: linear-gradient(135deg, #BF5AF2, #E2A2FF);
  }

  /* 8: 待面试 - 樱桃红 (重要日期提醒) */
  &.bg-status-8 {
    background: linear-gradient(135deg, #FF2D55, #FF5E7B);
  }

  /* 4: 办理中/送签中 - 靛蓝色 (使馆受理，最正式的阶段) */
  &.bg-status-4 {
    background: linear-gradient(135deg, #5856D6, #7D7BE5);
  }

  /* 5: 待收货/寄回中 - 薄荷绿/青色 (包裹即将到达的喜悦) */
  &.bg-status-5 {
    background: linear-gradient(135deg, #30B0C7, #5DD5D5);
  }

  /* 6: 已完成 - 苹果绿 (最终成功) */
  &.bg-status-6 {
    background: linear-gradient(135deg, #34C759, #32D74B);
  }

  .status-main {
    font-size: 24px;
    font-weight: 800;
    margin-bottom: 5px;
  }

  .status-desc {
    font-size: 13px;
    opacity: 0.8;
  }
}

.status-text {
  &.color-9 {
    color: #909399;
  }

  &.color-10 {
    color: #E6A23C;
  }
}

.drawer-body-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.inner-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.02);

  .card-label {
    font-size: 13px;
    font-weight: 700;
    color: #86868b;
    margin-bottom: 15px;
    text-transform: uppercase;
  }
}

.product-summary {
  display: flex;
  align-items: center;
  gap: 15px;

  .summary-img {
    width: 80px;
    height: 60px;
    border-radius: 10px;
    object-fit: cover;
  }

  h4 {
    font-size: 16px;
    margin-bottom: 5px;
    color: $text-main;
  }

  p {
    font-size: 12px;
    color: #999;
  }
}

/* 抽屉内时间胶囊 */
.drawer-slots {
  display: flex;
  flex-direction: column;
  gap: 10px;

  .drawer-time-capsule {
    background: #F7F9FC;
    border-radius: 12px;
    padding: 12px;
    text-align: center;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #EBF5FF;
      color: $primary-color;
    }
  }
}

/* 抽屉底部吸底 */
.drawer-footer {
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-top: 1px solid #eee;
  text-align: center;

  .drawer-main-btn {
    width: 100%;
    height: 50px;
    border-radius: 15px !important;
    font-size: 16px;
    font-weight: 700;
  }

  .contact-support {
    margin-top: 15px;
    font-size: 13px;
    color: $primary-color;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
}

.shipping-guide-card {
  background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%) !important;
  border: 1px solid #e6f0ff !important;

  .agency-address {
    padding: 10px 0;

    .address-item {
      font-size: 14px;
      margin-bottom: 8px;
      color: #444;
    }
  }

  .input-tip {
    font-size: 12px;
    color: #999;
    margin-bottom: 10px;
  }
}
</style>
