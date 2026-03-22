<template>
  <div class="app-container">
    <h2 style="margin-bottom: 20px; font-weight: 600; color: #303133;">签证业务审核工作台</h2>

    <!-- 1. 顶部标签页切换 -->
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
      <el-tab-pane label="待初审 (材料)" name="material">
        <span slot="label"><i class="el-icon-files"></i> 材料初审</span>
      </el-tab-pane>
      <el-tab-pane label="办理中/待面试" name="processing">
        <span slot="label"><i class="el-icon-s-flag"></i> 办理与面试</span>
      </el-tab-pane>
    </el-tabs>

    <!-- 2. 数据表格 -->
    <el-table v-loading="loading" :data="orderList" border stripe>
      <el-table-column label="订单号" align="center" prop="orderNo" width="180" />
      <el-table-column label="申请人" align="center" prop="customerId" width="120" :formatter="customerFormatter" />

      <!-- 显示产品快照里的标题 -->
      <el-table-column label="签证产品" align="center" width="250">
        <template slot-scope="scope">
          {{ parseSnapshot(scope.row.productSnapshot).title }}
        </template>
      </el-table-column>

      <el-table-column label="份数" align="center" prop="quantity" width="80" />
      <el-table-column label="提交时间" align="center" prop="updateTime" width="160" />

      <el-table-column label="当前状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ statusMap[scope.row.status] }}</el-tag>
        </template>
      </el-table-column>

      <!-- 新增：面试信息列 (插入在提交时间列和当前状态列之间) -->
      <el-table-column label="业务进展" align="center" width="220">
        <template slot-scope="scope">
          <!-- 状态 9：展示用户寄出的单号 -->
          <div v-if="scope.row.status === 9">
            <div style="color: #E6A23C; font-weight: bold;">
              <i class="el-icon-truck"></i> 客户寄送中
            </div>
            <div v-if="scope.row.expressToAgency" style="font-size: 12px; margin-top: 5px;">
              单号：{{ scope.row.expressToAgency }}
            </div>
            <div v-else style="font-size: 11px; color: #999;">等待客户填写单号</div>
          </div>
          <!-- 情况1：产品需要面试 -->
          <div v-if="scope.row.isInterviewRequired === 1">

            <!-- 如果用户还没选时间 (状态7) -->
            <div v-if="scope.row.status === 7 && !scope.row.interviewTime">
              <el-tag type="info" size="mini">等待用户选时间</el-tag>
              <div v-if="scope.row.interviewSlots" style="font-size: 11px; color: #999; margin-top: 5px;">
                已发方案: {{ parseJson(scope.row.interviewSlots).length }}个
              </div>
            </div>

            <!-- 如果用户已经选了时间 -->
            <div v-if="scope.row.interviewTime">
              <div style="font-weight: bold; color: #6AAFE6; font-size: 13px;">
                <i class="el-icon-time"></i> {{ scope.row.interviewTime }}
              </div>

              <!-- 如果管理员上传了预约单，显示查看链接 -->
              <div v-if="scope.row.interviewFile" style="margin-top: 5px;">
                <el-link type="success" :underline="false" @click="previewFile(scope.row.interviewFile)">
                  <i class="el-icon-document-checked"></i> 查看预约单
                </el-link>
              </div>
              <div v-else-if="scope.row.status === 8" style="font-size: 11px; color: #E6A23C; margin-top: 5px;">
                <i class="el-icon-warning-outline"></i> 待上传正式预约单
              </div>
            </div>

          </div>

          <!-- 情况2：不需要面试的产品 -->
          <div v-else>
            <span style="color: #ccc;">无需面试</span>
          </div>
        </template>
      </el-table-column>

      <!-- 操作列：根据 Tab 显示不同按钮 -->
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">

          <!-- Tab 1: 审核按钮 -->
          <el-button v-if="activeTab === 'material'" size="mini" type="primary" icon="el-icon-check"
            @click="handleAudit(scope.row)">审核材料</el-button>

          <!-- 在表格操作列 Tab 2 区域 -->
          <template v-if="activeTab === 'processing'">
            <!-- ★★★ 新增：查看材料按钮，让专员随时能看图 ★★★ -->
            <el-button size="mini" type="text" icon="el-icon-view" @click="handleAudit(scope.row)">查看材料</el-button>
            <el-button v-if="scope.row.status === 9" size="mini" type="success" icon="el-icon-receiving"
              @click="handleReceivePhysical(scope.row)">原件已收悉</el-button>
            <!-- 情况1：状态为7。无论有没有方案，都显示按钮。有方案叫“修改”，没方案叫“发送” -->
            <el-button v-if="scope.row.status === 7" size="mini" type="primary"
              :icon="scope.row.interviewSlots ? 'el-icon-edit' : 'el-icon-date'" @click="handleSendSlots(scope.row)">
              {{ scope.row.interviewSlots ? '修改预约方案' : '发送时间方案' }}
            </el-button>

            <!-- 情况2：状态为 8（用户选完了），显示上传 PDF 凭证 -->
            <el-button v-if="scope.row.status === 8 && !scope.row.interviewFile" size="mini" type="warning"
              icon="el-icon-upload" @click="handleUploadInterviewFile(scope.row)">上传正式预约单</el-button>

            <!-- 状态 8 或 4：通用录入最终结果 -->
            <el-button v-if="[8, 4].includes(scope.row.status)" size="mini" type="success" icon="el-icon-s-promotion"
              @click="handleResult(scope.row)">录入办理结果</el-button>


          </template>

        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- ================= 弹窗区域 ================= -->

    <!-- A. 材料审核弹窗 -->
    <el-dialog title="申请材料审核" :visible.sync="auditOpen" width="900px" append-to-body>
      <div class="dialog-content" v-if="currentOrder.id">
        <!-- 循环展示每一位申请人的材料 -->
        <div v-for="(person, idx) in parseMaterials(currentOrder.submittedMaterials)" :key="idx" class="person-box">
          <div class="person-title">第 {{ idx + 1 }} 位申请人</div>
          <el-descriptions border :column="1" size="small">
            <el-descriptions-item v-for="(val, key) in person" :key="key" :label="key">

              <!-- 1. 如果是图片：直接显示缩略图 -->
              <div v-if="isImage(val)">
                <el-image :src="getUrl(val)" :preview-src-list="[getUrl(val)]"
                  style="width: 100px; height: 70px; border-radius: 4px; border: 1px solid #eee; cursor: zoom-in;">
                </el-image>
              </div>

              <!-- 2. 如果是文件路径 (包含 /profile/upload)：显示下载/查看按钮 -->
              <div v-else-if="val && val.toString().indexOf('/profile/upload') > -1">
                <el-button type="primary" plain size="mini" :icon="getFileIcon(val)" @click="previewFile(val)">
                  {{ val.toLowerCase().includes('.pdf') ? '预览文件' : '下载查看' }}
                </el-button>
              </div>

              <!-- 3. 普通文字 -->
              <div v-else>{{ val }}</div>

            </el-descriptions-item>
          </el-descriptions>

          <!-- ★★★ 核心搬移：展示该申请人补交的材料 (针对Check) ★★★ -->
          <!-- 逻辑：只有当该申请人在 JSON 中有数据时才显示这个黄色虚线框 -->
          <div class="supp-viewer-section" v-if="getPersonSuppFiles(idx).length > 0">
            <div class="supp-header">
              <i class="el-icon-paperclip"></i> 该申请人补交的资料 (针对Check)
            </div>
            <div class="supp-file-grid">
              <div v-for="(fileUrl, fIdx) in getPersonSuppFiles(idx)" :key="fIdx" class="supp-file-item">
                <!-- 图片 -->
                <template v-if="isImage(fileUrl)">
                  <el-image :src="getUrl(fileUrl)" :preview-src-list="[getUrl(fileUrl)]" class="supp-img-mini" />
                </template>
                <!-- 文件 -->
                <template v-else>
                  <el-link type="primary" :underline="false" @click="previewFile(fileUrl)">
                    <i :class="getFileIcon(fileUrl)"></i> 附件 {{ fIdx + 1 }}
                  </el-link>
                </template>
              </div>
            </div>
          </div>

          <!-- 每个人的独立备注框 -->
          <div class="person-remark-input" v-if="auditRemarks[idx]">
            <div class="remark-label">对该申请人的审核意见：</div>
            <el-input type="textarea" v-model="auditRemarks[idx].text" placeholder="请输入审核意见" rows="2" />
            <!-- ★★★ 核心新增：针对个人的附件上传 ★★★ -->
            <div class="remark-file-upload">
              <el-upload :action="uploadUrl" :headers="headers"
                :on-success="(res) => handleRemarkUploadSuccess(res, idx)" :show-file-list="false">
                <el-button size="mini" type="info" plain icon="el-icon-upload2">
                  {{ auditRemarks[idx].file ? '已传附件(点击更换)' : '上传辅助说明文件' }}
                </el-button>
              </el-upload>
              <!-- 显示当前已上传的文件名 -->
              <div v-if="auditRemarks[idx].file" class="current-remark-file"
                style="margin-top: 5px; color: #6AAFE6; font-size: 13px;">
                <i class="el-icon-document"></i>
                <span>{{ getFileName(auditRemarks[idx].file) }}</span>
                <i class="el-icon-circle-close" style="cursor:pointer; margin-left:10px; color:#ff4949"
                  @click="auditRemarks[idx].file = ''"></i>
              </div>
            </div>

          </div>
        </div>

        <!-- 在审核弹窗中，展示基础材料的下方增加一块 -->
        <div v-if="currentOrder.supplementaryMaterials" class="supp-materials-section">
          <h4 class="sub-title" style="color: #E6A23C;"><i class="el-icon-paperclip"></i> 补充材料（Check后提交）</h4>

          <div v-for="(files, pIdx) in parseJson(currentOrder.supplementaryMaterials)" :key="pIdx"
            class="supp-person-row">
            <span class="p-tag">申请人 {{ parseInt(pIdx) + 1 }}:</span>
            <div class="file-list">
              <el-link v-for="(fileUrl, fIdx) in files" :key="fIdx" type="primary" @click="previewFile(fileUrl)"
                style="margin-right: 15px;">
                查看附件 {{ fIdx + 1 }}
              </el-link>
            </div>
          </div>
        </div>

        <div style="margin-top: 20px;">
          <el-input type="textarea" v-model="auditRemark" placeholder="请输入审核备注（如驳回原因）" rows="3"></el-input>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <!-- 情况1：材料初审阶段 (状态2) -->
        <template v-if="activeTab === 'material'">
          <el-button type="success" @click="submitAudit(4)">初步合格</el-button>
          <el-button type="danger" @click="submitAudit(3)">材料驳回</el-button>
        </template>

        <!-- 情况2：办理/Check阶段 (状态4, 7, 8) -->
        <template v-else>
          <!-- ★★★ 核心新增：保存并同步意见 ★★★ -->
          <!-- 即使不改变状态，也可以更新 audit_remark 里的分人评语 -->
          <el-button type="primary" plain @click="updateAuditRemarksOnly">更新修改建议</el-button>
          <span style="font-size: 12px; color: #909399; margin-right: 15px; margin-left: 10px;">
            注：此操作将更新用户的修改指引，不改变订单状态
          </span>
        </template>
        <el-button @click="auditOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- B. 结果录入弹窗 -->
    <el-dialog title="签证办理进度管理" :visible.sync="resultOpen" width="700px">
      <div v-if="currentOrder.id">
        <!-- 每个人独立的结果录入 -->
        <div v-for="(person, idx) in individualResults" :key="idx" class="result-row-item">
          <div class="p-info">申请人 {{ idx + 1 }}</div>
          <div class="p-action">
            <el-radio-group v-model="person.status" size="small">
              <el-radio :label="1">通过</el-radio>
              <el-radio :label="2">拒签</el-radio>
              <el-radio :label="3">Check</el-radio>
            </el-radio-group>
            <el-input v-model="person.memo" placeholder="备注内容" size="mini" style="width:150px; margin-left:10px" />
          </div>
        </div>

        <el-divider content-position="left">物流信息（仅在准备寄回时填写）</el-divider>
        <el-form label-width="80px">
          <el-form-item label="快递单号">
            <el-input v-model="resultForm.trackingNumber" placeholder="若尚未出签，请留空" />
          </el-form-item>
        </el-form>
      </div>

      <div slot="footer">
        <!-- ★ 按钮 1：只存结果，不改订单状态，依然留在“办理中” -->
        <el-button @click="submitOnlyResult">仅保存结果</el-button>

        <!-- ★ 按钮 2：存结果并改状态为 5 (待收货) -->
        <el-button type="primary" @click="submitFinalResult">录入并确认发货</el-button>
      </div>
    </el-dialog>


    <!-- C. 面试预约方案下发弹窗 -->
    <el-dialog title="配置面试预约方案" :visible.sync="slotOpen" width="450px" append-to-body>
      <div class="slot-config-container">
        <p style="color: #909399; font-size: 13px; margin-bottom: 15px;">
          <i class="el-icon-info"></i> 该产品需要面签，请提供 2-3 个可选时间供用户确认。
        </p>

        <div v-for="(item, index) in slotList" :key="index"
          style="display: flex; align-items: center; margin-bottom: 10px;">
          <el-date-picker v-model="slotList[index]" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
            format="yyyy-MM-dd HH:mm" placeholder="选择备选时间点" size="small" style="flex: 1" />
          <el-button type="text" icon="el-icon-delete" style="color: #F56C6C; margin-left: 10px"
            @click="slotList.splice(index, 1)" v-if="slotList.length > 1"></el-button>
        </div>

        <el-button type="dashed" icon="el-icon-plus" @click="slotList.push('')" style="width: 100%; margin-top: 10px;">
          增加备选时间
        </el-button>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitInterviewSlots">发送方案给用户</el-button>
        <el-button @click="slotOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 上传正式预约单弹窗 -->
    <el-dialog title="上传面试预约确认单" :visible.sync="fileOpen" width="400px" append-to-body>
      <div style="text-align: center; padding: 20px;">
        <p style="margin-bottom: 20px; color: #666;">请上传从使馆官网下载的 PDF 或确认单截图</p>

        <el-upload :action="uploadUrl" :headers="headers" :on-success="handleFileSuccess" :limit="1"
          :show-file-list="true">
          <el-button size="medium" type="primary" icon="el-icon-upload">选择文件并上传</el-button>
        </el-upload>

        <div v-if="fileForm.interviewFile" style="margin-top: 15px; color: #6AAFE6;">
          <i class="el-icon-circle-check"></i> 文件已就绪
        </div>
      </div>
      <div slot="footer">
        <el-button @click="fileOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitFile">确 认 下 发</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listOrder, updateOrder } from "@/api/visa/order";

export default {
  name: "OrderAudit",
  data() {
    return {
      loading: true,
      orderList: [],
      total: 0,
      activeTab: 'material',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        status: 2,
        params: {} // 确保初始化时有 params
      },
      auditOpen: false,
      currentOrder: {},
      auditRemark: '',
      auditRemarks: [],
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      headers: { Authorization: "Bearer " + localStorage.getItem('Admin-Token') }, // 修正B端Token获取
      resultOpen: false,
      resultForm: {
        visaResult: 1,
        trackingNumber: ''
      },
      statusMap: {
        0: '待支付', 1: '待上传', 2: '待审核', 3: '需补交', 4: '办理中', 5: '待收货', 6: '已完成', 7: '材料过审，待预约面试', 8: '待面试', 9: '待寄送原件'
      },
      slotOpen: false,    // 面试方案弹窗
      slotList: ['', ''], // 初始化两个空时间点
      fileOpen: false,
      fileForm: { id: null, interviewFile: '' },
      individualResults: [],
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 1. Tab 切换逻辑 - 彻底隔离参数 */
    handleTabClick(tab) {
      this.queryParams.pageNum = 1;
      // 重置所有过滤参数
      this.queryParams.status = null;
      this.queryParams.params = {};

      if (tab.name === 'material') {
        this.queryParams.status = 2;
      } else if (tab.name === 'processing') {
        // 利用我们在 XML 写的自定义 SQL 逻辑
        this.$set(this.queryParams.params, 'auditType', 'processing');
      }
      this.getList();
    },

    getList() {
      this.loading = true;
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => { this.loading = false; });
    },

    /** 2. 审核弹窗打开逻辑 - 增加极强容错 */
    handleAudit(row) {
      // 1. 深度克隆数据
      this.currentOrder = JSON.parse(JSON.stringify(row));

      // 2. 先计算出有几个人
      const materials = this.parseMaterials(this.currentOrder.submittedMaterials);
      const applicantCount = materials.length;

      // 3. 【核心】先创建一个临时的、完整的备注数组
      let tempRemarks = [];
      try {
        if (this.currentOrder.auditRemark && this.currentOrder.auditRemark.startsWith('[')) {
          const saved = JSON.parse(this.currentOrder.auditRemark);
          tempRemarks = saved.map(item =>
            (typeof item === 'object' ? item : { text: item, file: "" })
          );
        }
      } catch (e) {
        console.log("解析旧备注失败，准备初始化新数组");
      }

      // 4. 如果解析出来的长度不对，或者没数据，补齐它
      if (tempRemarks.length !== applicantCount) {
        tempRemarks = [];
        for (let i = 0; i < applicantCount; i++) {
          tempRemarks.push({ text: "", file: "" });
        }
      }

      // 5. ★ 最后一步：一次性赋值给响应式变量，并打开弹窗 ★
      this.auditRemarks = tempRemarks;
      this.auditOpen = true;
    },

    /** 3. 提交审核 - 逻辑流转 */
    submitAudit(targetStatus) {
      if (targetStatus === 4) { // 点击的是“通过”
        this.$confirm('该签证是否需要客户邮寄护照原件？', '寄送确认', {
          distinguishCancelAndClose: true,
          confirmButtonText: '需要寄送',
          cancelButtonText: '无需寄送(电子签)',
          type: 'warning'
        })
          .then(() => {
            // A方案：需要寄送，状态转为 9
            this.executeStatusUpdate(9, "初审通过，已通知客户寄送原件");
          })
          .catch(action => {
            if (action === 'cancel') {
              // B方案：无需寄送，走原有的 4 或 7 逻辑
              let finalStatus = this.currentOrder.isInterviewRequired === 1 ? 7 : 4;
              this.executeStatusUpdate(finalStatus, "初审通过，已进入后续环节");
            }
          });
      } else {
        // 驳回逻辑保持不变
        this.executeStatusUpdate(3, "已驳回申请");
      }
    },

    // 抽离出的更新方法
    executeStatusUpdate(status, msg) {
      const data = {
        id: this.currentOrder.id,
        status: status,
        auditRemark: JSON.stringify(this.auditRemarks)
      };
      updateOrder(data).then(res => {
        this.$modal.msgSuccess(msg);
        this.auditOpen = false;
        this.getList();
      });
    },

    /** 4. 打开录入办理结果弹窗 */
    handleResult(row) {
      // 1. 深度克隆当前订单数据
      this.currentOrder = JSON.parse(JSON.stringify(row));

      // 2. 初始化分人结果列表 (核心：根据已提交的材料数量来决定有几个人)
      const materials = this.parseMaterials(this.currentOrder.submittedMaterials);

      // 3. 准备 individualResults 数组
      // 如果数据库里已经有结果了(说明是二次修改)，就回显；否则初始化
      if (this.currentOrder.visaResult && this.currentOrder.visaResult.startsWith('[')) {
        this.individualResults = JSON.parse(this.currentOrder.visaResult);
      } else {
        this.individualResults = materials.map(() => ({
          status: 1, // 默认选“出签”
          memo: ""   // 备注留空
        }));
      }

      this.resultForm.trackingNumber = this.currentOrder.trackingNumber || "";
      this.resultOpen = true;
    },

    /** 4. 录入结果逻辑 */
    // 逻辑 A：仅保存结果（方便同步 Check 状态给用户）
    submitOnlyResult() {
      const data = {
        id: this.currentOrder.id,
        visaResult: JSON.stringify(this.individualResults)
        // 注意：这里不传 status，数据库状态保持 4 或 7
      };
      updateOrder(data).then(res => {
        this.$modal.msgSuccess("结果已同步给客户，订单仍处于办理中");
        this.resultOpen = false;
        this.getList();
      });
    },

    // 逻辑 B：正式发货（流程流转到 5）
    /** 提交最终结果并决定是否发货 */
    submitFinalResult() {
      const results = this.individualResults;

      // 1. 检查是否有人还在 Check 状态
      const hasChecking = results.some(item => item.status == 3);
      if (hasChecking) {
        return this.$message.warning("仍有申请人处于审查(Check)中，请先使用‘仅保存结果’，待全员出结论后再发货结单。");
      }

      // 2. 判断是否有人出签
      const hasApproved = results.some(item => item.status == 1);

      let finalStatus = 6; // 默认：直接结案
      let successMsg = "所有申请人均被拒签，订单已自动结案";

      if (hasApproved) {
        // 只要有一个人过了，就必须填单号并走状态 5
        if (!this.resultForm.trackingNumber) {
          return this.$message.error("订单中包含过签护照，请务必填写快递单号");
        }
        finalStatus = 5;
        successMsg = "结果录入成功，已进入待收货环节";
      }

      // 3. 构造提交数据
      const data = {
        id: this.currentOrder.id,
        status: finalStatus, // 智能分流后的状态
        visaResult: JSON.stringify(this.individualResults),
        trackingNumber: this.resultForm.trackingNumber
      };

      updateOrder(data).then(res => {
        this.$modal.msgSuccess(successMsg);
        this.resultOpen = false;
        this.getList();
      });
    },

    /** 辅助：从备注中提取用户上传的反馈图路径 */
    getFeedbackImg(remark) {
      // 1. 判空
      if (!remark) return null;

      // 2. 检查是否包含约定的前缀
      const prefix = "凭证路径：";
      const index = remark.indexOf(prefix);

      if (index !== -1) {
        // 3. 截取路径部分
        let path = remark.substring(index + prefix.length).trim();

        // 4. 如果路径本身是空的
        if (!path || path === 'null' || path === 'undefined') return null;

        // 5. 拼上后端地址 (重点：确保能访问到)
        // 如果路径没带 http，就拼上 process.env.VUE_APP_BASE_API
        if (path.indexOf('http') === -1) {
          return process.env.VUE_APP_BASE_API + path;
        }
        return path;
      }
      return null;
    },

    /** 5. 发送面试方案逻辑 */
    submitInterviewSlots() {
      // 过滤掉没选时间的空行
      const finalSlots = this.slotList.filter(s => s && s.trim() !== '');
      if (finalSlots.length < 1) {
        return this.$message.warning("请至少设置一个备选时间点");
      }

      const data = {
        id: this.currentOrder.id,
        status: 7, // ★ 状态流转为：待面试
        interviewSlots: JSON.stringify(finalSlots), // 存入 JSON 数组
        auditRemark: JSON.stringify(this.auditRemarks) // 顺便存入通过的备注
      };

      updateOrder(data).then(res => {
        this.$modal.msgSuccess("面试预约方案已下发");
        this.slotOpen = false;
        this.auditOpen = false; // 两个弹窗全关
        this.getList();
      });
    },

    /** 6. 打开发送时间方案弹窗 */
    handleSendSlots(row) {
      this.currentOrder = JSON.parse(JSON.stringify(row));

      // ★★★ 核心修复：如果是修改，把旧的 JSON 重新转回数组给 slotList ★★★
      if (this.currentOrder.interviewSlots) {
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

    /** 打开上传弹窗 */
    handleUploadInterviewFile(row) {
      this.fileForm = { id: row.id, interviewFile: row.interviewFile };
      this.fileOpen = true;
    },

    /** 上传成功回调 */
    handleFileSuccess(res) {
      if (res.code === 200) {
        this.fileForm.interviewFile = res.fileName;
        this.$message.success("上传成功");
      }
    },

    /** 提交到数据库 */
    submitFile() {
      if (!this.fileForm.interviewFile) {
        return this.$message.error("请先上传文件");
      }
      updateOrder(this.fileForm).then(res => {
        this.$modal.msgSuccess("预约单已下发给客户");
        this.fileOpen = false;
        this.getList();
      });
    },

    handleReceivePhysical(row) {
      this.$confirm('确认已收到该客户的护照原件并核对无误？', '入库确认').then(() => {
        // 收到原件后，根据是否需面试跳到 7 或 4
        let nextStatus = row.isInterviewRequired === 1 ? 7 : 4;
        updateOrder({ id: row.id, status: nextStatus }).then(res => {
          this.$modal.msgSuccess("原件已入库，流程继续");
          this.getList();
        });
      });
    },
    // --- 辅助工具 ---
    getUrl(url) {
      if (!url) return '';
      return process.env.VUE_APP_BASE_API + url;
    },
    handleRemarkUploadSuccess(res, idx) {
      if (res.code === 200) {
        this.$set(this.auditRemarks[idx], 'file', res.fileName);
        this.$modal.msgSuccess("附件上传成功");
      }
    },
    getFileName(url) {
      if (!url) return "";
      return url.substring(url.lastIndexOf("/") + 1);
    },
    /** 解析 JSON 字符串 */
    parseJson(str) {
      if (!str) return [];
      try {
        // 如果已经是对象则直接返回，否则解析
        return typeof str === 'string' ? JSON.parse(str) : str;
      } catch (e) {
        console.error("JSON解析失败:", e);
        return [];
      }
    },
    parseSnapshot(json) {
      try { return JSON.parse(json || '{}') } catch (e) { return { title: '数据解析异常' } }
    },
    parseMaterials(json) {
      try { return JSON.parse(json || '[]') } catch (e) { return [] }
    },
    parseAddress(json) {
      try {
        const addr = JSON.parse(json || '{}');
        return `${addr.contactName} (${addr.contactPhone}) - ${addr.address}`;
      } catch (e) { return '地址未填写' }
    },
    isImage(val) {
      if (!val || typeof val !== 'string') return false;
      const lower = val.toLowerCase();
      return ['.jpg', '.jpeg', '.png', '.gif'].some(ext => lower.endsWith(ext));
    },
    getFileIcon(val) {
      const lower = (val || '').toLowerCase();
      if (lower.endsWith('.pdf')) return 'el-icon-document';
      if (lower.includes('doc')) return 'el-icon-document-copy';
      return 'el-icon-paperclip';
    },
    previewFile(url) {
      window.open(this.getUrl(url), '_blank');
    },
    getStatusType(status) {
      const map = { 2: 'danger', 4: 'primary', 7: 'warning', 5: 'success' };
      return map[status] || 'info';
    },
    customerFormatter(row) {
      return "客户ID: " + row.customerId;
    },
    /** 获取特定申请人的补充材料数组 */
    getPersonSuppFiles(index) {
      if (!this.currentOrder.supplementaryMaterials) return [];
      try {
        // 数据库存的是 {"1": ["url1"], "2": ["url2"]}
        const allSuppData = JSON.parse(this.currentOrder.supplementaryMaterials);
        // 注意：JSON 的 key 是字符串，需要转一下索引
        return allSuppData[index.toString()] || [];
      } catch (e) {
        return [];
      }
    },
    /** ★★★ 仅更新审核意见 (不流转状态) ★★★ */
    updateAuditRemarksOnly() {
      const data = {
        id: this.currentOrder.id,
        // 将当前弹窗里修改后的数组重新打包成 JSON
        auditRemark: JSON.stringify(this.auditRemarks)
      };

      updateOrder(data).then(res => {
        this.$modal.msgSuccess("反馈已更新，用户可立即看到您的新要求");
        // 可以在此处顺便发一条留言板消息提醒用户
        this.auditOpen = false;
      });
    },
    handleReceivePhysical(row) {
      this.$confirm(`确认已收到订单 ${row.orderNo} 的护照原件并核对无误吗？`, '入库确认', {
        confirmButtonText: '确认入库',
        cancelButtonText: '取消',
        type: 'success'
      }).then(() => {
        // 逻辑：收到原件后，判断该产品是否需要面试
        let nextStatus = row.isInterviewRequired === 1 ? 7 : 4;

        const data = {
          id: row.id,
          status: nextStatus,
          remark: "管理员已签收实体材料，流程继续"
        };

        updateOrder(data).then(res => {
          this.$modal.msgSuccess("签收成功，订单已进入下一步");
          this.getList(); // 刷新列表
        });
      });
    }
  }
};
</script>

<style scoped>
.person-box {
  margin-bottom: 20px;
  border: 1px solid #EBEEF5;
  padding: 15px;
  border-radius: 4px;
}

.person-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #409EFF;
  background: #ecf5ff;
  padding: 5px 10px;
  display: inline-block;
  border-radius: 4px;
}

.result-dialog-container {
  padding: 0 10px;

  .sub-title {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 15px;
    color: #2c3e50;

    i {
      color: #6AAFE6;
      margin-right: 5px;
    }
  }
}

.user-feedback-section {
  background: #fdf6ec;
  padding: 15px;
  border-radius: 12px;

  .feedback-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
    font-size: 13px;
  }
}

.result-row-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f7fa;

  &:last-child {
    border-bottom: none;
  }

  .p-info {
    font-weight: 600;
    color: #606266;
  }
}

.supp-viewer-section {
  margin-top: 15px;
  background-color: #fdf6ec;
  /* 淡淡的警告黄，提醒审核员关注 */
  border: 1px dashed #e6a23c;
  border-radius: 8px;
  padding: 12px;
}

.supp-header {
  font-size: 13px;
  font-weight: 700;
  color: #e6a23c;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.supp-file-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.supp-img-mini {
  width: 80px;
  height: 60px;
  border-radius: 6px;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: zoom-in;
}
</style>