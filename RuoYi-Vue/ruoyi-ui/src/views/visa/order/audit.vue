<template>
  <div class="app-container">
    <h2 style="margin-bottom: 20px; font-weight: 600; color: #303133;">签证业务审核工作台</h2>

    <!-- 1. 顶层 Tab：按任务属性拆分 -->
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
      <el-tab-pane name="material">
        <span slot="label"><i class="el-icon-edit-outline"></i> 1. 材料初审 (2)</span>
      </el-tab-pane>
      <el-tab-pane name="physical">
        <span slot="label">
          <el-badge :value="physicalCount" :hidden="physicalCount === 0" class="item">
            <i class="el-icon-box"></i> 2. 原件签收
          </el-badge>
        </span>
      </el-tab-pane>
      <el-tab-pane name="processing">
        <span slot="label"><i class="el-icon-loading"></i> 3. 办理中</span>
      </el-tab-pane>
      <el-tab-pane name="completed">
        <span slot="label"><i class="el-icon-finished"></i> 4. 结案订单</span>
      </el-tab-pane>
    </el-tabs>

    <!-- 2. 数据表格 -->
    <el-table v-loading="loading" :data="orderList" border stripe>
      <el-table-column label="订单号" align="center" prop="orderNo" width="180" />
      <el-table-column label="签证产品" align="center" width="220">
        <template slot-scope="scope">
          {{ parseSnapshot(scope.row.productSnapshot).title }}
          <el-tag size="mini" effect="plain" v-if="scope.row.isPhysicalRequired === 1" type="danger">需原件</el-tag>
        </template>
      </el-table-column>

      <!-- 业务进展列：动态展示当前任务 -->
      <el-table-column label="业务进展" align="center" min-width="200">
        <template slot-scope="scope">
          <div v-if="scope.row.status === 2" style="color: #409EFF">等待审核电子材料</div>
          <div v-else-if="scope.row.status === 9" style="color: #909399">已通知客户寄送，等待单号</div>
          <div v-else-if="scope.row.status === 10">
            <el-tag type="warning" size="mini">快递在途中</el-tag>
            <div style="font-size: 12px; margin-top:5px">单号: {{ scope.row.expressToAgency }}</div>
          </div>
          <div v-else-if="scope.row.status === 7">

            <!-- 场景 A：用户【已确认】时间 -->
            <!-- 逻辑：只要 interviewTime 有值，就代表协商达成，隐藏备注，显示已选时间 -->
            <div v-if="scope.row.interviewTime">
              <div
                style="color: #67C23A; font-weight: bold; border: 1px solid #67C23A; padding: 4px 8px; border-radius: 6px; display: inline-block; background: #F6FFED;">
                <i class="el-icon-time"></i> 用户已选: {{ scope.row.interviewTime }}
              </div>
              <div style="margin-top: 8px;">
                <el-button size="mini" type="warning" icon="el-icon-upload"
                  @click="handleUploadInterviewFile(scope.row)">
                  上传正式预约单
                </el-button>
              </div>
            </div>

            <!-- 场景 B：用户【未确认】时间 且 【有反馈留言】 -->
            <!-- 逻辑：协商中，且客户提出了要求，显示红框提醒审核员重新发方案 -->
            <div v-else-if="scope.row.userRemark"
              style="background: #FFF1F0; border: 1px solid #FFA39E; padding: 8px; border-radius: 8px; margin-bottom: 8px;">
              <div style="color: #F5222D; font-size: 11px; font-weight: bold; margin-bottom: 3px;">
                <i class="el-icon-chat-dot-round"></i> 客户要求：
              </div>
              <div style="color: #666; font-size: 12px; line-height: 1.4;">{{ scope.row.userRemark }}</div>
              <el-button size="mini" type="primary" plain icon="el-icon-date" @click="handleSendSlots(scope.row)"
                style="margin-top:5px">
                重新发送方案
              </el-button>
            </div>

            <!-- 场景 C：用户【未确认】时间 且 【没留过言】 -->
            <!-- 逻辑：初始下发状态，等待客户操作 -->
            <div v-else>
              <el-tag type="info" size="mini">等待用户确认时间</el-tag>
              <div style="margin-top: 8px;">
                <el-button size="mini" type="text" icon="el-icon-edit" @click="handleSendSlots(scope.row)">
                  修改/重发方案
                </el-button>
              </div>
            </div>

          </div>
          
          <div v-else-if="scope.row.status === 8" style="color: #67C23A">面试中: {{ scope.row.interviewTime }}</div>
          <div v-else-if="scope.row.status === 4" style="color: #E6A23C">使馆办理中...</div>
          <div v-else-if="scope.row.status === 5" style="color: #909399">结果已出，正在寄回</div>
          <div v-else style="color: #ccc">流程已终结</div>
        </template>
      </el-table-column>

      <el-table-column label="当前状态" align="center" width="120">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="small">{{ statusMap[scope.row.status] }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <!-- Tab 1 动作 -->
          <el-button v-if="scope.row.status === 2" size="mini" type="primary"
            @click="handleAudit(scope.row, false)">去审核</el-button>

          <!-- Tab 2 动作 -->
          <el-button v-if="scope.row.status === 10" size="mini" type="success" icon="el-icon-receiving"
            @click="handleConfirmEntry(scope.row)">确认收货入库</el-button>

          <!-- Tab 3 动作 -->
          <template v-if="['processing'].includes(activeTab)">
            <el-button size="mini" type="text" @click="handleAudit(scope.row, true)">查看材料</el-button>
            <el-button v-if="scope.row.status === 7" size="mini" type="text"
              @click="handleSendSlots(scope.row)">发方案</el-button>
              <el-button v-if="scope.row.status === 7 && scope.row.interviewTime" size="mini" type="text"
              style="color: #E6A23C;" @click="handleUploadInterviewFile(scope.row)">
              上传预约确认单
            </el-button>
            <el-button v-if="[4, 8].includes(scope.row.status)" size="mini" type="text"
              @click="handleResult(scope.row)">录结果</el-button>
          </template>

          <!-- Tab 4 动作 -->
          <el-button v-if="['completed'].includes(activeTab)" size="mini" type="text"
            @click="handleAudit(scope.row, true)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 审核弹窗 -->
    <!-- 1. 审核弹窗：重构为左右布局 -->
    <el-dialog :title="isReadOnly ? '材料详情查看' : '申请材料审核工作台'" :visible.sync="auditOpen" width="1000px" append-to-body
      custom-class="audit-dialog-custom">

      <div class="audit-container" v-if="currentOrder.id">
        <!-- 左侧：申请人切换栏 -->
        <div class="audit-aside">
          <div class="aside-header">申请人名单 ({{ applicantListInDialog.length }})</div>
          <div v-for="(app, index) in applicantListInDialog" :key="index"
            :class="['app-nav-item', { 'is-active': activeAppIndex === index }]" @click="activeAppIndex = index">
            <div class="app-name">{{ app.name || '申请人 ' + (index + 1) }}</div>
            <div class="app-tag">{{ getIdentityLabel(app.identity) }}</div>
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>

        <!-- 右侧：材料详情区 -->
        <div class="audit-main">
          <div class="detail-header">
            正在审核：<span>{{ currentApplicant.name }}</span> 的材料
          </div>

          <div class="material-list-scroll">
            <!-- 循环展示当前申请人的所有材料项 -->
            <div v-for="(val, key) in currentApplicantMaterials" :key="key" class="material-block">
              <div class="material-label">{{ key }}：</div>
              <div class="material-content">
                <!-- 图片预览 -->
                <div v-if="isImage(val)">
                  <el-image :src="getUrl(val)" :preview-src-list="[getUrl(val)]" class="material-img">
                    <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
                  </el-image>
                </div>
                <!-- 文件下载 -->
                <div v-else-if="isFile(val)">
                  <el-link type="primary" :underline="false" @click="previewFile(val)">
                    <i class="el-icon-document"></i> 点击预览/下载附件
                  </el-link>
                </div>
                <!-- 纯文本 -->
                <div v-else class="text-val">{{ val || '未填写' }}</div>
              </div>
            </div>

            <div v-if="getPersonSuppFiles(activeAppIndex).length > 0" class="supp-materials-section">
              <div class="supp-title">
                <i class="el-icon-paperclip"></i> 该申请人补交的资料 (针对Check)
              </div>

              <div class="supp-file-list">
                <div v-for="(fileUrl, fIdx) in getPersonSuppFiles(activeAppIndex)" :key="fIdx" class="supp-file-card">
                  <div class="file-info">
                    <i :class="getFileIcon(fileUrl)"></i>
                    <span class="file-name">附件 {{ fIdx + 1 }}</span>
                  </div>
                  <div class="file-actions">
                    <!-- 预览按钮 -->
                    <el-button type="text" size="mini" icon="el-icon-view" @click="previewFile(fileUrl)">预览</el-button>
                    <!-- 图片类支持点击直接看大图 -->
                    <el-image v-if="isImage(fileUrl)" style="width: 0; height: 0; position: absolute;"
                      :src="getUrl(fileUrl)" :preview-src-list="[getUrl(fileUrl)]" ref="suppImage"></el-image>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部意见区 -->
          <div class="remark-section" v-if="auditRemarks && auditRemarks[activeAppIndex]">
            <div class="remark-title">针对该申请人的审核意见：</div>
            <el-input type="textarea" v-model="auditRemarks[activeAppIndex].text" :rows="3"
              :placeholder="isReadOnly ? '暂无意见' : '若材料有问题，请在此处写明修改要求...'" :disabled="isReadOnly"></el-input>
          </div>
        </div>
      </div>

      <!-- 弹窗底部：全局决策按钮 -->
      <div slot="footer" class="dialog-footer">
        <template v-if="!isReadOnly">
          <el-button type="success" size="medium" icon="el-icon-circle-check" @click="submitFirstAudit">初步合格</el-button>
          <el-button type="danger" size="medium" icon="el-icon-circle-close"
            @click="executeStatusUpdate(3, '已驳回')">材料驳回</el-button>
        </template>
        <el-button @click="auditOpen = false" size="medium">{{ isReadOnly ? '关 闭' : '取 消' }}</el-button>
      </div>
    </el-dialog>

    <!-- B. 结果录入弹窗 -->
    <el-dialog title="签证办理进度管理" :visible.sync="resultOpen" width="800px" append-to-body custom-class="result-dialog">
      <div v-if="currentOrder.id" class="result-manage-container">
        <div class="tip-banner">
          <i class="el-icon-info"></i> 请根据使馆实际反馈录入结果。若包含过签护照，请在下方填写寄回单号。
        </div>

        <!-- 申请人卡片列表 -->
        <div class="applicants-grid">
          <div v-for="(person, idx) in individualResults" :key="idx" class="applicant-result-card">
            <div class="card-header">
              <span class="app-index">申请人 {{ idx + 1 }}</span>
              <span class="app-name" v-if="applicantListInDialog[idx]">{{ applicantListInDialog[idx].name }}</span>
            </div>

            <div class="card-body">
              <el-row :gutter="20">
                <!-- 左侧：状态选择 -->
                <el-col :span="10">
                  <div class="form-label">办理结果</div>
                  <el-radio-group v-model="person.status" size="medium" class="status-radio-group">
                    <el-radio-button :label="1">获签</el-radio-button>
                    <el-radio-button :label="2">拒签</el-radio-button>
                    <el-radio-button :label="3">Check</el-radio-button>
                  </el-radio-group>
                </el-col>

                <!-- 右侧：备注输入 -->
                <el-col :span="14">
                  <div class="form-label">结果备注 (可选)</div>
                  <el-input v-model="person.memo" placeholder="例如：给1年多次" size="small" />
                </el-col>
              </el-row>

              <!-- 下方：凭证上传区-->
              <div class="evidence-section">
                <div class="evidence-header">
                  <!-- 动态显示标题 -->
                  <span>
                    {{ person.status == 1 ? '获签凭证 / 准签信' : (person.status == 2 ? '官方拒签信' : '行政审查通知单') }}
                  </span>
                  <span>
                    <!-- 增加判断：如果选了 Check (3)，文字变为下发通知单 -->
                    {{ person.status == 3 ? '下发【补件通知单/指引】' : (person.status == 1 ? '上传准签页' : '上传拒签信') }}
                  </span>

                  <el-upload :action="uploadUrl" :headers="headers"
                    :on-success="(res) => handleResultFileUpload(res, idx)" :show-file-list="false"
                    class="inline-upload">
                    <el-button size="mini" type="primary" plain icon="el-icon-upload2">
                      {{ person.fileUrl ? '更换文件' : '点击上传' }}
                    </el-button>
                  </el-upload>
                </div>

                <!-- 预览区 -->
                <div v-if="person.fileUrl" class="evidence-preview">
                  <div class="preview-item">
                    <el-image v-if="isImage(person.fileUrl)" :src="getUrl(person.fileUrl)"
                      :preview-src-list="[getUrl(person.fileUrl)]" class="thumb-img"
                      :class="{ 'border-red': person.status == 2, 'border-green': person.status == 1 }">
                    </el-image>
                    <div v-else class="file-icon-box" @click="previewFile(person.fileUrl)">
                      <i class="el-icon-document" :style="{ color: person.status == 2 ? '#F56C6C' : '#409EFF' }"></i>
                      <span>查看{{ person.status == 2 ? '拒签' : '获签' }}文件</span>
                    </div>
                    <i class="el-icon-error delete-btn" @click="person.fileUrl = ''"></i>
                  </div>
                </div>

                
              </div>
            </div>
          </div>
        </div>

        <el-divider><i class="el-icon-truck"></i> 物流与发货</el-divider>

        <!-- 物流表单区域 -->
        <div class="logistics-section">
          <el-form label-position="left" label-width="100px" size="medium">
            <el-row :gutter="40">
              <el-col :span="12">
                <el-form-item label="快递公司">
                  <el-select v-model="resultForm.courierCompany" placeholder="请选择" style="width: 100%">
                    <el-option label="顺丰速运" value="顺丰速运" />
                    <el-option label="EMS" value="EMS" />
                    <el-option label="中通快递" value="中通快递" />
                    <el-option label="圆通速递" value="圆通速递" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="快递单号">
                  <el-input v-model="resultForm.trackingNumber" placeholder="录入寄回单号" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="resultOpen = false" round>取 消</el-button>
        <el-button @click="submitOnlyResult" plain type="primary" round>仅保存结果</el-button>
        <el-button type="primary" @click="submitFinalResult" round>录入并确认发货</el-button>
      </div>
    </el-dialog>
    <!-- C. 配置面试预约方案弹窗 -->
    <el-dialog title="配置面试预约方案" :visible.sync="slotOpen" width="450px" append-to-body>
      <div class="slot-config-container">
        <p style="color: #F56C6C; font-size: 13px; margin-bottom: 15px;">
          <i class="el-icon-date"></i> 请提供 2-3 个可选时间点供用户确认。
        </p>

        <div v-for="(item, index) in slotList" :key="index"
          style="display: flex; align-items: center; margin-bottom: 10px;">
          <el-date-picker v-model="slotList[index]" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
            format="yyyy-MM-dd HH:mm" placeholder="选择备选时间" size="small" style="flex: 1" />
          <el-button type="text" icon="el-icon-delete" style="color: #F56C6C; margin-left: 10px"
            @click="slotList.splice(index, 1)" v-if="slotList.length > 1"></el-button>
        </div>

        <el-button type="dashed" icon="el-icon-plus" @click="slotList.push('')"
          style="width: 100%; margin-top: 10px;">增加备选时间</el-button>
      </div>
      <div slot="footer">
        <el-button type="primary" @click="submitInterviewSlots">发送方案给用户</el-button>
        <el-button @click="slotOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- ★★★ D. 上传正式预约单弹窗 (补回丢失的 HTML) ★★★ -->
    <el-dialog title="上传面试预约确认单" :visible.sync="fileOpen" width="420px" append-to-body custom-class="apple-dialog">
      <div style="text-align: center; padding: 20px 10px;">
        <div style="margin-bottom: 20px; font-size: 13px; color: #606266; line-height: 1.5;">
          <i class="el-icon-info" style="color: #409EFF; margin-bottom: 8px; font-size: 24px; display: block;"></i>
          用户已确认时间，请去使馆官网完成预约，<br/>并在此处上传官方下发的 PDF 或截图凭证。
        </div>

        <div v-if="fileForm.interviewFile" class="file-preview-box" style="margin-bottom: 20px;">
          <div class="preview-item">
            <!-- 如果是图片 -->
            <el-image v-if="isImage(fileForm.interviewFile)" :src="getUrl(fileForm.interviewFile)"
              :preview-src-list="[getUrl(fileForm.interviewFile)]"
              style="width: 100px; height: 100px; border-radius: 8px; border: 1px solid #ddd;" />
            <!-- 如果是文件 -->
            <div v-else class="file-icon-box" @click="previewFile(fileForm.interviewFile)">
              <i class="el-icon-document" style="font-size: 30px; color: #409EFF;"></i>
              <p style="font-size: 12px; margin-top: 5px;">查看已上传文件</p>
            </div>
            <!-- 删除按钮 -->
            <i class="el-icon-circle-close delete-btn" @click="fileForm.interviewFile = ''"></i>
          </div>
        </div>

        <!-- 上传组件 -->
        <el-upload 
          :action="uploadUrl" 
          :headers="headers" 
          :on-success="handleFileSuccess" 
          :limit="1"
          :show-file-list="false"
        >
          <el-button size="medium" type="primary" plain icon="el-icon-upload">
            {{ fileForm.interviewFile ? '已上传 (点击更换)' : '选择文件并上传' }}
          </el-button>
        </el-upload>

        <!-- 上传成功的视觉反馈 -->
        <div v-if="fileForm.interviewFile" style="margin-top: 15px; color: #67C23A; font-weight: bold; font-size: 14px;">
          <i class="el-icon-circle-check"></i> 文件已准备就绪
        </div>
      </div>
      <div slot="footer">
        <el-button @click="fileOpen = false" round>取 消</el-button>
        <el-button type="primary" @click="submitFile" round>确 认 下 发</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
/* eslint-disable */
import { listOrder, updateOrder, approveOrder } from "@/api/visa/order";
import request from '@/utils/request';

export default {
  data() {
    return {
      activeTab: 'material',
      loading: true,
      orderList: [],
      total: 0,
      physicalCount: 0,
      queryParams: { pageNum: 1, pageSize: 10, status: 2, params: {} },
      auditOpen: false,
      currentOrder: {},
      isReadOnly: false,
      auditRemarks: [],
      statusMap: {
        2: '待初审', 3: '需补交', 9: '待寄原件', 10: '已寄送',
        7: '待发方案', 8: '待面试', 4: '办理中', 5: '待收货', 6: '已完成'
      },
      activeAppIndex: 0, // 当前选中的申请人索引
      applicantListInDialog: [], // 存储解析出的申请人及材料
      // 1. 上传地址 (RuoYi 通用上传接口)
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      // 2. 请求头 (上传必须带 Token，否则后端报 401)
      headers: {
        Authorization: "Bearer " + localStorage.getItem('Admin-Token')
      },

      // 3. 结果录入相关的控制
      resultOpen: false,
      resultForm: {
        trackingNumber: '',
        courierCompany: '顺丰速运'

      },
      individualResults: [],
      slotList: [{ time: '' }, { time: '' }],
      userremark: '',
      fileOpen: false,
      fileForm: { id: null, interviewFile: '' },
    };
  },
  computed: {
    // 当前选中的申请人对象
    currentApplicant() {
      return this.applicantListInDialog[this.activeAppIndex] || {};
    },
    // 当前选中的申请人提交的材料项
    currentApplicantMaterials() {
      return this.currentApplicant.materials || {};
    }
  },
  created() {
    this.getList();
    this.refreshPhysicalBadge();
  },
  methods: {
    handleTabClick(tab) {
      this.queryParams.pageNum = 1;
      this.queryParams.status = null;
      this.queryParams.params = {};

      const tabMap = {
        'material': { status: 2 },
        'physical': { status: 10 },
        'processing': { auditType: 'processing' },
        'completed': { auditType: 'completed' }
      };

      const config = tabMap[tab.name];
      if (config.status) this.queryParams.status = config.status;
      if (config.auditType) this.$set(this.queryParams.params, 'auditType', config.auditType);

      this.getList();
    },

    /** 核心：打开审核弹窗逻辑（唯一定义） */
    handleAudit(row, readonly = false) {
      this.isReadOnly = readonly;
      this.activeAppIndex = 0;
      // 1. 深度克隆行数据
      this.currentOrder = JSON.parse(JSON.stringify(row));

      // 2. 解析核心数据
      // materialsArray 是用户提交的 JSON 数组：[ {项1:值, 项2:值}, {人2数据} ]
      const materialsArray = this.parseMaterials(this.currentOrder.submittedMaterials);
      // applicants 是数据库申请人子表的数据
      const applicants = this.currentOrder.applicantList || [];

      // 3. 构造 UI 所需的复合数组：将“人”的信息和“材料”对应起来
      // 如果没有申请人列表（旧订单兼容），则按材料份数生成
      if (applicants.length > 0) {
        this.applicantListInDialog = applicants.map((app, index) => ({
          ...app,
          materials: materialsArray[index] || {}
        }));
      } else {
        // 兼容性逻辑：防止页面全空
        this.applicantListInDialog = materialsArray.map((m, index) => ({
          name: `申请人 ${index + 1}`,
          identity: 'EMPLOYED',
          materials: m
        }));
      }

      // 4. 处理备注的回显逻辑
      let savedRemarks = [];
      try {
        if (this.currentOrder.auditRemark && this.currentOrder.auditRemark.startsWith('[')) {
          savedRemarks = JSON.parse(this.currentOrder.auditRemark);
        }
      } catch (e) {
        console.log("解析旧备注失败");
      }

      // 初始化备注数组，长度必须与申请人一致
      this.auditRemarks = this.applicantListInDialog.map((item, index) => {
        // 如果已存有对象形式的备注则取 text，否则取字符串
        const oldRem = savedRemarks[index];
        if (oldRem && typeof oldRem === 'object') return { text: oldRem.text || "", file: oldRem.file || "" };
        return { text: typeof oldRem === 'string' ? oldRem : "", file: "" };
      });

      this.auditOpen = true;
    },

    /** 身份标签转换 */
    getIdentityLabel(id) {
      const map = {
        'EMPLOYED': '在职',
        'STUDENT': '学生',
        'CHILD': '学龄前',
        'RETIRED': '退休',
        'FREELANCE': '自由职业',
        'UNEMPLOYED': '无业/主妇'
      };
      return map[id] || '普通申请人';
    },

    /** 获取完整资源路径 */
    getUrl(url) {
      if (!url) return '';
      return url.startsWith('http') ? url : (process.env.VUE_APP_BASE_API + url);
    },

    /** 判定是否为图片 */
    isImage(val) {
      if (!val || typeof val !== 'string') return false;
      const lower = val.toLowerCase();
      return ['.jpg', '.jpeg', '.png', '.gif', '.webp'].some(ext => lower.endsWith(ext));
    },

    /** 判定是否为文件路径 */
    isFile(val) {
      if (!val || typeof val !== 'string') return false;
      return val.indexOf('/profile/upload') > -1 && !this.isImage(val);
    },

    /** 文件预览/下载 */
    previewFile(url) {
      window.open(this.getUrl(url), '_blank');
    },

    /** 自动化初审通过逻辑 */
    submitFirstAudit() {
      this.executeStatusUpdate(true); // 直接发起通过请求
    },

    executeStatusUpdate(isPass) {
      const loading = this.$loading({ text: '处理中...' });

      if (isPass) {
        // 情况 A：初审通过 -> 调用专用审批接口
        // 注意：这里的 URL 要对应你 Controller 新加的路径
        approveOrder(this.currentOrder.id, { auditRemark: JSON.stringify(this.auditRemarks) }).then(res => {
          loading.close();
          this.$modal.msgSuccess("初审已通过");
          this.auditOpen = false;
          this.getList();
          this.refreshPhysicalBadge();
        });
      } else {
        // 情况 B：驳回 -> 依然可以调通用的 updateOrder 改为状态 3
        updateOrder({
          id: this.currentOrder.id,
          status: 3,
          auditRemark: JSON.stringify(this.auditRemarks)
        }).then(() => {
          loading.close();
          this.$modal.msgSuccess("已驳回申请");
          this.auditOpen = false;
          this.getList();
        });
      }
    },

    handleConfirmEntry(row) {
      this.$confirm(`确认已收到订单 ${row.orderNo} 的纸质原件吗？`, '入库确认').then(() => {
        let nextStatus = row.isInterviewRequired === 1 ? 7 : 4;
        updateOrder({ id: row.id, status: nextStatus, remark: "原件已入库" }).then(() => {
          this.$modal.msgSuccess("签收成功");
          this.getList();
          this.refreshPhysicalBadge();
        });
      });
    },

    refreshPhysicalBadge() {
      listOrder({ status: 10 }).then(res => { this.physicalCount = res.total; });
    },

    getList() {
      this.loading = true;
      listOrder(this.queryParams).then(res => {
        this.orderList = res.rows;
        this.total = res.total;
        this.loading = false;
      });
    },

    parseMaterials(json) { try { return JSON.parse(json || '[]') } catch (e) { return [] } },
    parseSnapshot(json) { try { return JSON.parse(json || '{}') } catch (e) { return {} } },
    getStatusType(s) {
      const map = { 2: 'danger', 10: 'warning', 4: 'primary', 6: 'success' };
      return map[s] || 'info';
    },
    customerFormatter(row) { return "客户ID: " + row.customerId; },

    /** 4. 打开录入办理结果弹窗 */
    handleResult(row) {
      // 1. 深度克隆行数据
      this.currentOrder = JSON.parse(JSON.stringify(row));

      // 2. ★★★ 核心修复：在这里同步初始化申请人名单数据 ★★★
      // 只有初始化了它，Template 里的 applicantListInDialog[idx].name 才能被读到
      const applicants = this.currentOrder.applicantList || [];
      const materialsArray = this.parseMaterials(this.currentOrder.submittedMaterials);

      if (applicants.length > 0) {
        this.applicantListInDialog = applicants.map((app, index) => ({
          ...app,
          materials: materialsArray[index] || {}
        }));
      } else {
        // 兼容性处理：如果没有子表数据，至少保证能显示“申请人 1”
        this.applicantListInDialog = materialsArray.map((m, index) => ({
          name: `申请人 ${index + 1}`,
          identity: 'EMPLOYED'
        }));
      }

      const applicantCount = this.applicantListInDialog.length;

      // 3. 初始化或回显办理结果（1-获签, 2-拒签, 3-Check）
      if (this.currentOrder.visaResult && this.currentOrder.visaResult.startsWith('[')) {
        this.individualResults = JSON.parse(this.currentOrder.visaResult);
        
        // 健壮性处理：确保旧数据里每个对象都有 fileUrl 属性，防止报错
        this.individualResults.forEach(item => {
          if (item.fileUrl === undefined) this.$set(item, 'fileUrl', '');
        });
      } else {
        this.individualResults = [];
        for (let i = 0; i < applicantCount; i++) {
          // 初始化默认值，增加 fileUrl 字段
          this.individualResults.push({ status: 1, memo: "", fileUrl: "" });
        }
      }

      // 4. 确保 resultForm 存在再赋值
      if (!this.resultForm) {
        this.resultForm = { trackingNumber: "", courierCompany: "顺丰速运" };
      }
      this.resultForm.trackingNumber = this.currentOrder.trackingNumber || "";
      
      // 5. 打开弹窗
      this.resultOpen = true;
    },
    /** 5. 录入结果逻辑：正式发货/结案 */
    submitFinalResult() {
      const results = this.individualResults;

      // 1. 业务逻辑检查：如果有任何一人处于 Check 状态，不能直接发货结案
      const hasChecking = results.some(item => item.status == 3);
      if (hasChecking) {
        return this.$message.warning("仍有申请人处于审查(Check)中，请先通过‘仅保存结果’同步进度，待全员出结论后再操作。");
      }

      // 2. 判定后续流程分流
      // 只要有一个人“通过”，订单就要进入 5 (待收货) 并要求填单号；如果全员“拒签”，直接进入 6 (已完成)
      const hasApproved = results.some(item => item.status == 1);

      let finalStatus = 6; // 默认：全员拒签直接结案
      let successMsg = "处理完成，由于全员拒签，订单已自动结案";

      if (hasApproved) {
        if (!this.resultForm.trackingNumber) {
          return this.$message.error("订单中包含已通过的护照，请务必填写寄回的快递单号");
        }
        finalStatus = 5; // 进入待收货
        successMsg = "结果录入成功，已进入待收货环节";
      }

      const data = {
        id: this.currentOrder.id,
        orderNo: this.currentOrder.orderNo,
        status: finalStatus,
        visaResult: JSON.stringify(this.individualResults),
        trackingNumber: this.resultForm.trackingNumber,
        params: {
          courierCompany: this.resultForm.courierCompany
        }
      };

      updateOrder(data).then(res => {
        this.$modal.msgSuccess(successMsg);
        this.resultOpen = false;
        this.getList();
      });
    },

    /** 6. 打开发送时间方案弹窗 */
    handleSendSlots(row) {
      this.currentOrder = JSON.parse(JSON.stringify(row));

      // 如果已经发过方案（修改模式），解析 JSON；否则初始化空行
      if (this.currentOrder.interviewSlots && this.currentOrder.interviewSlots.startsWith('[')) {
        try {
          this.slotList = JSON.parse(this.currentOrder.interviewSlots);
        } catch (e) {
          this.slotList = ['', ''];
        }
      } else {
        this.slotList = ['', ''];
      }
      this.slotOpen = true;
    },

    /** 7. 发送面试方案逻辑 */
    submitInterviewSlots() {
      // 过滤掉没选时间的空行
      const finalSlots = this.slotList.filter(s => s && s.trim() !== '');
      if (finalSlots.length < 1) {
        return this.$message.warning("请至少提供一个备选时间点");
      }

      const data = {
        id: this.currentOrder.id,
        status: 7, // 状态：待确认面试
        interviewSlots: JSON.stringify(finalSlots)
      };

      updateOrder(data).then(res => {
        this.$modal.msgSuccess("面试预约方案已下发，请等待用户在微信/网页端确认");
        this.slotOpen = false;
        this.getList();
      });
    },

    /** 补充：仅保存结果不改变订单状态 (针对 Check 过程中的进度同步) */
    submitOnlyResult() {
      const data = {
        id: this.currentOrder.id,
        visaResult: JSON.stringify(this.individualResults)
      };
      updateOrder(data).then(res => {
        this.$modal.msgSuccess("结果已同步给客户，订单进度已更新");
        this.resultOpen = false;
        this.getList();
      });
    },

    handleResultFileUpload(res, idx) {
      if (res.code === 200) {
        // 动态给这个人的结果对象加一个 fileUrl 属性
        this.$set(this.individualResults[idx], 'fileUrl', res.fileName);
        this.$message.success(`申请人 ${idx + 1} 的凭证上传成功`);
      }
    },

    handleConfirmTime(order, selectedTime) {
      this.$confirm(`您确认选择 ${selectedTime} 参加面试吗？确认后将不可更改。`, '面试时间确认', {
        confirmButtonText: '确定选择',
        cancelButtonText: '再想想',
        type: 'info'
      }).then(() => {
        const loading = this.$loading({ text: '正在锁定面试名额...' });

        // 构造更新数据
        const data = {
          id: order.id,
          interviewTime: selectedTime, // 选定的最终时间
          status: 8  // 状态 7 -> 8 (待面试)
        };

        // ★ 关键：确认你的接口路径是否正确
        request({
          url: '/client/order/update',
          method: 'put',
          data: data
        }).then(res => {
          loading.close();
          if (res.code === 200) {
            this.$message.success('预约时间确认成功！');
            this.selectedOrder.status = 8;
            this.selectedOrder.interviewTime = selectedTime;
            this.getList(); // 刷新列表
          }
        }).catch(() => { loading.close(); });
      });
    },

    handleUploadInterviewFile(row) {
      this.fileForm = {
        id: row.id,
        interviewFile: row.interviewFile || ''
      };
      this.fileOpen = true;
    },

    /** 提交预约单并正式转入“待面试”状态 */
    submitFile() {
      if (!this.fileForm.interviewFile) {
        return this.$message.error("请先上传预约确认单 PDF 或截图");
      }

      const data = {
        id: this.fileForm.id,
        interviewFile: this.fileForm.interviewFile,
        // ★ 核心逻辑：只有上传了正式预约单，订单才算正式进入“待面试”状态
        status: 8,
        remark: "管理员已确认面试预约，并下发官方确认单"
      };

      updateOrder(data).then(res => {
        this.$modal.msgSuccess("预约单已下发，订单状态已变为：待面试");
        this.fileOpen = false;
        this.getList(); // 刷新列表，订单会从“待确认”视觉变为“面试中”
      });
    },

    /** 上传成功的回调 */
    handleFileSuccess(res) {
      if (res.code === 200) {
        this.fileForm.interviewFile = res.fileName;
        this.$message.success("文件上传成功");
      }
    },

    /** 获取当前选中申请人的补交材料数组 */
    getPersonSuppFiles(index) {
      if (!this.currentOrder.supplementaryMaterials) return [];
      try {
        // 数据库存的是 {"0": ["url1", "url2"], "1": ["url3"]}
        // 注意：JSON 的 key 是字符串格式的索引
        const allData = JSON.parse(this.currentOrder.supplementaryMaterials);
        const myFiles = allData[index.toString()];
        return Array.isArray(myFiles) ? myFiles : [];
      } catch (e) {
        console.error("解析补交材料失败", e);
        return [];
      }
    },

    /** 获取文件图标逻辑（增强版） */
    getFileIcon(url) {
      if (this.isImage(url)) return 'el-icon-picture';
      if (url.toLowerCase().endsWith('.pdf')) return 'el-icon-document';
      return 'el-icon-files';
    }
  }
};
</script>

<style scoped>
.audit-container {
  display: flex;
  height: 600px;
  border: 1px solid #EBEEF5;
}

/* 左侧名单列表 */
.audit-aside {
  width: 200px;
  border-right: 1px solid #EBEEF5;
  background-color: #F8F9FB;
  overflow-y: auto;
}

.aside-header {
  padding: 15px;
  font-size: 13px;
  font-weight: bold;
  color: #909399;
  border-bottom: 1px solid #EBEEF5;
}

.app-nav-item {
  padding: 20px 15px;
  cursor: pointer;
  border-bottom: 1px solid #F0F2F5;
  position: relative;
  transition: all 0.3s;
}

.app-nav-item:hover {
  background-color: #EDF2FC;
}

.app-nav-item.is-active {
  background-color: #FFFFFF;
  border-right: 3px solid #409EFF;
}

.app-name {
  font-weight: bold;
  font-size: 15px;
  margin-bottom: 5px;
}

.app-tag {
  font-size: 12px;
  color: #909399;
}

.app-nav-item i {
  position: absolute;
  right: 10px;
  top: 30px;
  color: #C0C4CC;
}

/* 右侧内容详情 */
.audit-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background-color: #FFF;
}

.detail-header {
  font-size: 18px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #F5F7FA;
}

.detail-header span {
  color: #409EFF;
  font-weight: bold;
}

.material-list-scroll {
  flex: 1;
  overflow-y: auto;
  padding-right: 10px;
}

.material-block {
  margin-bottom: 25px;
}

.material-label {
  font-weight: bold;
  color: #606266;
  margin-bottom: 10px;
}

.material-img {
  width: 200px;
  height: 140px;
  border-radius: 8px;
  border: 1px solid #DCDFE6;
}

.text-val {
  padding: 10px;
  background: #F5F7FA;
  border-radius: 4px;
  color: #333;
}

/* 意见输入区 */
.remark-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px dashed #DCDFE6;
}

.remark-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #E6A23C;
}

/* 弹窗整体容器 */
.result-manage-container {
  padding: 0 10px;
}

/* 顶部提示横条 */
.tip-banner {
  background-color: #f4f4f5;
  color: #909399;
  padding: 10px 15px;
  border-radius: 8px;
  font-size: 13px;
  margin-bottom: 20px;
}

/* 申请人卡片样式 */
.applicant-result-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  margin-bottom: 20px;
  background: #fff;
  overflow: hidden;
  transition: all 0.3s;
}

.applicant-result-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.card-header {
  background: #f8fbff;
  padding: 10px 15px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
}

.app-index {
  font-size: 12px;
  color: #909399;
  font-weight: bold;
  text-transform: uppercase;
  margin-right: 10px;
}

.app-name {
  font-size: 15px;
  font-weight: 600;
  color: #409EFF;
}

.card-body {
  padding: 15px;
}

.form-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

/* 凭证上传区 */
.evidence-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
}

.evidence-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
  color: #606266;
}

.evidence-preview {
  display: flex;
  gap: 10px;
}

.preview-item {
  position: relative;
  width: 80px;
  height: 60px;
}

.thumb-img {
  width: 100%;
  height: 100%;
  border-radius: 6px;
  border: 1px solid #dcdfe6;
}

.file-icon-box {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid #dcdfe6;
}

.file-icon-box i { font-size: 20px; color: #409EFF; }
.file-icon-box span { font-size: 10px; color: #999; margin-top: 2px; }

.delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  color: #F56C6C;
  font-size: 18px;
  cursor: pointer;
  background: #fff;
  border-radius: 50%;
}

/* 物流部分 */
.logistics-section {
  padding: 10px 0;
}

/* 调整单选按钮样式 */
::v-deep .el-radio-button__inner {
  padding: 8px 15px;
}

.border-red {
  border: 2px solid #F56C6C !important; /* 拒签信红色边框 */
}
.border-green {
  border: 2px solid #67C23A !important; /* 准签信绿色边框 */
}

/* 补件材料区整体样式 */
.supp-materials-section {
  margin-top: 30px;
  padding: 15px;
  background-color: #FFFBE6; /* 警示黄背景 */
  border: 1px dashed #FFE58F; /* 虚线边框 */
  border-radius: 12px;
}

.supp-title {
  font-size: 14px;
  font-weight: bold;
  color: #856404;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.supp-file-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}

.supp-file-card {
  background: #fff;
  padding: 10px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #f0f0f0;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #333;
}

.file-info i {
  font-size: 18px;
  color: #409EFF;
}

.file-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>