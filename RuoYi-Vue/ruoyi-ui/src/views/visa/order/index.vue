<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerId">
        <el-select v-model="queryParams.customerId" placeholder="请选择/搜索客户" clearable filterable @change="handleQuery">
          <el-option v-for="item in customerList" :key="item.id"
            :label="item.realName || item.nickName || item.userName || item.id" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="产品名称" prop="productId">
        <el-select v-model="queryParams.productId" placeholder="请选择/搜索产品" clearable filterable @change="handleQuery">
          <el-option v-for="item in productList" :key="item.id"
            :label="item.title || item.productName || item.name || item.id" :value="item.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="流水号" prop="alipayNo">
        <el-input v-model="queryParams.alipayNo" placeholder="请输入流水号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">

      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['visa:order:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['visa:order:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['visa:order:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['visa:order:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单ID" align="center" prop="id" width="80" />
      <el-table-column label="订单编号" align="center" prop="orderNo" min-width="200" />
      <el-table-column label="客户名称" align="center" prop="customerId" min-width="120">
        <template slot-scope="scope">
          <span>{{ getCustomerName(scope.row.customerId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="签证产品" align="center" prop="productId" min-width="180" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <span>{{ getProductName(scope.row.productId) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="订单数量" align="center" prop="quantity" width="80" />
      <el-table-column label="申请人" align="center">
        <template slot-scope="scope">
          <el-popover placement="right" width="300" trigger="hover">
            <el-table :data="scope.row.applicantList" size="mini">
              <el-table-column property="name" label="姓名"></el-table-column>
              <el-table-column property="passportNo" label="护照号" width="100"></el-table-column>
              <el-table-column property="idCard" label="身份证号" width="150"></el-table-column>
            </el-table>
            <el-button slot="reference" type="text">{{ getFirstApplicantName(scope.row) }}</el-button>
          </el-popover>
        </template>
      </el-table-column>

      <!-- JSON 快照查看方案 -->
      <el-table-column label="产品快照" align="center" prop="productSnapshot" width="100">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view"
            @click="handleViewSnapshot(scope.row.productSnapshot, '产品快照')">查看</el-button>
        </template>
      </el-table-column>

      <el-table-column label="总金额" align="center" prop="totalAmount" width="100" />

      <!-- 订单状态 9级字典转换 -->
      <el-table-column label="订单状态" align="center" prop="status" width="300">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="流水号" align="center" prop="alipayNo" min-width="250" :show-overflow-tooltip="true" />
      <el-table-column label="支付时间" align="center" prop="payTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.payTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>

      <!-- 复杂的 JSON 材料查看方案 -->
      <el-table-column label="签证材料" align="center" prop="submittedMaterials" width="100">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-folder-opened"
            @click="handleViewMaterials(scope.row.submittedMaterials, scope.row)">查看配置</el-button>
        </template>
      </el-table-column>

      <el-table-column label="审核反馈" align="center" width="120">
        <template slot-scope="scope">
          <div v-if="hasAuditFeedback(scope.row.auditRemark)">
            <el-button size="mini" type="text" icon="el-icon-chat-line-round"
              @click="handleViewAuditRecords(scope.row)">查看反馈</el-button>
          </div>
          <span v-else class="text-muted">暂无反馈</span>
        </template>
      </el-table-column>

      <el-table-column label="面试预约时间" align="center" prop="interviewTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.interviewTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>

      <!-- 文件直接预览组件化 -->
      <el-table-column label="面试预约单" align="center" prop="interviewFile" width="120">
        <template slot-scope="scope">
          <div v-if="scope.row.interviewFile">
            <el-image v-if="isImage(scope.row.interviewFile)" style="width: 40px; height: 40px; border-radius: 4px;"
              :src="resolveUrl(scope.row.interviewFile)" :preview-src-list="[resolveUrl(scope.row.interviewFile)]">
            </el-image>
            <el-link v-else type="primary" :underline="false" target="_blank"
              :href="resolveUrl(scope.row.interviewFile)">
              <i class="el-icon-document"></i> 预览下载
            </el-link>
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <!-- 面试反馈状态映射 -->
      <el-table-column label="面试反馈" align="center" width="160">
        <template slot-scope="scope">
          <!-- 判定是否有反馈数据 -->
          <el-popover v-if="hasInterviewFeedback(scope.row.interviewFeedback)" placement="left" width="350"
            trigger="hover">
            <!-- 悬浮窗：展示每位申请人的具体面试反馈 -->
            <el-table :data="formatInterviewFeedback(scope.row)" size="mini" border stripe>
              <el-table-column label="申请人" prop="name" width="120" align="center"></el-table-column>
              <el-table-column label="面试结果" align="center">
                <template slot-scope="s">
                  <el-tag :type="getFeedbackTagType(s.row.result)" size="mini">
                    {{ getFeedbackText(s.row.result) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>

            <!-- 表格内显示的标签摘要 -->
            <div slot="reference">
              <el-tag v-for="(item, i) in parseJson(scope.row.interviewFeedback)" :key="i"
                :type="getFeedbackTagType(item.result)" size="mini" effect="plain" style="margin: 2px;">
                {{ getFeedbackText(item.result) }}
              </el-tag>
            </div>
          </el-popover>
          <span v-else class="text-muted">未反馈</span>
        </template>
      </el-table-column>

      <!-- 最终签证结果状态映射 -->
      <el-table-column label="签证结果" align="center" width="160">
        <template slot-scope="scope">
          <!-- 只有当有结果数据时才显示 -->
          <el-popover v-if="hasVisaResult(scope.row.visaResult)" placement="left" width="400" trigger="hover">
            <!-- 悬浮窗内容：以表格形式展示每个人的结果 -->
            <el-table :data="formatVisaResult(scope.row)" size="mini" border stripe>
              <el-table-column label="申请人" prop="name" width="100" align="center"></el-table-column>
              <el-table-column label="结果" width="80" align="center">
                <template slot-scope="s">
                  <el-tag :type="getResultTagType(s.row.status)" size="mini">
                    {{ getResultText(s.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="凭证" width="80" align="center">
                <template slot-scope="s">
                  <div v-if="s.row.fileUrl">
                    <!-- A. 如果是图片：使用 el-image 自带的预览功能，最直观 -->
                    <el-image v-if="isImage(s.row.fileUrl)"
                      style="width: 30px; height: 30px; border-radius: 4px; cursor: pointer; border: 1px solid #eee;"
                      :src="resolveUrl(s.row.fileUrl)" :preview-src-list="[resolveUrl(s.row.fileUrl)]" append-to-body>
                      <div slot="error" class="image-slot"><i class="el-icon-picture"></i></div>
                    </el-image>

                    <!-- B. 如果是文件（如PDF）：显示图标点击打开新窗口 -->
                    <el-link v-else type="primary" :underline="false" @click="previewFile(s.row.fileUrl)">
                      <i class="el-icon-document" style="font-size: 20px;"></i>
                    </el-link>
                  </div>
                  <span v-else class="text-muted">-</span>
                </template>
              </el-table-column>
              <el-table-column label="备注" prop="memo" show-overflow-tooltip></el-table-column>
            </el-table>

            <!-- 表格单元格内显示的简略摘要 -->
            <div slot="reference" class="result-summary-tags">
              <el-tag v-for="(item, i) in parseJson(scope.row.visaResult)" :key="i"
                :type="getResultTagType(item.status)" size="mini" effect="dark" style="margin: 2px;">
                {{ getResultText(item.status) }}
              </el-tag>
            </div>
          </el-popover>
          <span v-else class="text-muted">办理中...</span>
        </template>
      </el-table-column>


      <!-- 地址 JSON 快照查看 -->
      <el-table-column label="收货地址" align="center" prop="mailingAddress" width="100">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-map-location"
            @click="handleViewSnapshot(scope.row.mailingAddress, '收货地址详情')">查看地址</el-button>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['visa:order:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['visa:order:remove']">删除</el-button>
          <el-button size="mini" type="text" icon="el-icon-chat-dot-round"
            @click="handleContactCustomer(scope.row)">联系客户</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改签证订单对话框 (不变) -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <!-- 为了避免冲突，编辑表单保留原生输入形式 -->
        <el-form-item label="订单编号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入订单编号" />
        </el-form-item>
        <el-form-item label="客户ID" prop="customerId">
          <el-input v-model="form.customerId" placeholder="请输入客户ID" />
        </el-form-item>
        <el-form-item label="产品ID" prop="productId">
          <el-input v-model="form.productId" placeholder="请输入产品ID" />
        </el-form-item>
        <el-form-item label="订单数量" prop="quantity">
          <el-input-number v-model="form.quantity" placeholder="请输入订单数量" :min="1" />
        </el-form-item>
        <el-form-item label="产品快照" prop="productSnapshot">
          <el-input v-model="form.productSnapshot" type="textarea" placeholder="请输入内容(JSON)" />
        </el-form-item>
        <el-form-item label="总金额" prop="totalAmount">
          <el-input v-model="form.totalAmount" placeholder="请输入订单总金额" />
        </el-form-item>
        <el-form-item label="流水号" prop="alipayNo">
          <el-input v-model="form.alipayNo" placeholder="请输入支付宝流水号" />
        </el-form-item>
        <el-form-item label="支付时间" prop="payTime">
          <el-date-picker clearable v-model="form.payTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="请选择时间"></el-date-picker>
        </el-form-item>
        <el-form-item label="用户动态材料" prop="submittedMaterials">
          <el-input v-model="form.submittedMaterials" type="textarea" placeholder="请输入内容(JSON)" />
        </el-form-item>
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input v-model="form.auditRemark" type="textarea" placeholder="请输入驳回原因/备注" />
        </el-form-item>
        <el-form-item label="预约面试时间" prop="interviewTime">
          <el-date-picker clearable v-model="form.interviewTime" type="date" value-format="yyyy-MM-dd"
            placeholder="请选择面试时间"></el-date-picker>
        </el-form-item>

        <el-form-item label="面试预约单" prop="interviewFile">
          <file-upload v-model="form.interviewFile" />
        </el-form-item>
        <el-form-item label="面试反馈" prop="interviewFeedback">
          <!-- 这里给您做了字典优化 -->
          <el-select v-model="form.interviewFeedback" placeholder="请选择测试反馈">
            <el-option label="0 未反馈" value="0"></el-option>
            <el-option label="1 过签" value="1"></el-option>
            <el-option label="2 拒签" value="2"></el-option>
            <el-option label="3 Check" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="最终签证结果" prop="visaResult">
          <el-select v-model="form.visaResult" placeholder="请选择最终结果">
            <el-option label="1 出签" value="1"></el-option>
            <el-option label="2 拒签" value="2"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- ====== 视图抽屉/弹窗：解析 JSON ====== -->
    <!-- 1. 通用快照详情（对象） -->
    <el-dialog :title="snapshotTitle" :visible.sync="snapshotOpen" width="500px" append-to-body>
      <el-descriptions border :column="1" size="small">
        <el-descriptions-item v-for="(value, key) in currentSnapshotObj" :key="key"
          :label="snapshotLabelMap[key] || addressLabelMap[key] || key">
          <!-- 动态支持快照内的图像或文件URL -->
          <div v-if="isValidUrl(String(value))">
            <el-image v-if="isImage(String(value))" style="width: 60px; height: 60px; border-radius: 4px;"
              :src="resolveUrl(String(value))" :preview-src-list="[resolveUrl(String(value))]">
              <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
            </el-image>
            <el-link v-else type="primary" target="_blank" :href="resolveUrl(String(value))">
              <i class="el-icon-document"></i> 点击预览/下载
            </el-link>
          </div>
          <span v-else>{{ value }}</span>
        </el-descriptions-item>
        <div v-if="Object.keys(currentSnapshotObj).length === 0" style="text-align:center; padding: 20px;">
          暂无结构化数据或格式不正确
        </div>
      </el-descriptions>
      <div slot="footer"><el-button @click="snapshotOpen = false">关闭</el-button></div>
    </el-dialog>

    <!-- 2. 材料详情（数组表格式） -->
    <el-dialog title="用户上传的材料明细" :visible.sync="materialsOpen" width="850px" append-to-body>
      <el-table :data="currentMaterialsArray" border stripe height="500px">
        <el-table-column label="申请人" prop="applicantName" width="120" align="center">
          <template slot-scope="scope">
            <el-tag size="small">{{ scope.row.applicantName }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="材料项" prop="materialKey" width="150" align="center" />

        <el-table-column label="上传内容预览" align="center">
          <template slot-scope="scope">
            <div v-if="scope.row.materialValue">
              <!-- 判定是否为路径，如果是路径则显示预览/下载 -->
              <div v-if="isValidUrl(String(scope.row.materialValue))">
                <el-image v-if="isImage(String(scope.row.materialValue))"
                  style="width: 80px; height: 60px; border-radius: 4px; border: 1px solid #eee;"
                  :src="resolveUrl(scope.row.materialValue)" :preview-src-list="[resolveUrl(scope.row.materialValue)]">
                  <div slot="error" class="image-slot"><i class="el-icon-picture-outline"></i></div>
                </el-image>
                <el-link v-else type="primary" :underline="false" target="_blank"
                  :href="resolveUrl(scope.row.materialValue)">
                  <i class="el-icon-document"></i> 预览/下载文件
                </el-link>
              </div>
              <!-- 否则显示纯文本内容 -->
              <span v-else>{{ scope.row.materialValue }}</span>
            </div>
            <span v-else class="text-muted">未提交</span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer"><el-button @click="materialsOpen = false">关闭</el-button></div>
    </el-dialog>

    <!-- 审核记录详情弹窗 -->
    <el-dialog title="审核意见明细" :visible.sync="auditOpen" width="500px" append-to-body>
      <el-table :data="currentAuditRecords" border stripe size="medium">
        <!-- 改为显示申请人姓名 -->
        <el-table-column label="申请人" prop="name" width="120" align="center">
          <template slot-scope="scope">
            <el-tag size="small" type="info">{{ scope.row.name }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="反馈意见内容" prop="text">
          <template slot-scope="scope">
            <span style="white-space: pre-wrap; color: #606266;">{{ scope.row.text }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="auditOpen = false">关 闭</el-button>
      </div>
    </el-dialog>



  </div>
</template>

<script>
import { addOrder, delOrder, getOrder, listOrder, updateOrder } from "@/api/visa/order";
import { listCustomer } from "@/api/visa/customer";
import { listProduct } from "@/api/visa/product";

export default {
  name: "Order",
  data() {
    return {
      // 遮罩层
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      orderList: [],
      title: "",
      open: false,

      // ====== 新增：外键相关缓存数据 ======
      customerList: [],
      productList: [],

      // ====== 新增：弹窗控制 ======
      snapshotOpen: false,
      snapshotTitle: '详情',
      currentSnapshotObj: {},

      materialsOpen: false,
      currentMaterialsArray: [],

      auditOpen: false,          // 审核弹窗开关
      currentAuditRecords: [],   // 存储解析后的审核数组

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        customerId: null,
        productId: null,
        status: null,
        alipayNo: null,
        trackingNumber: null,
      },
      form: {},
      rules: {
        orderNo: [{ required: true, message: "订单编号不能为空", trigger: "blur" }],
        customerId: [{ required: true, message: "客户ID不能为空", trigger: "blur" }],
        productId: [{ required: true, message: "产品ID不能为空", trigger: "blur" }],
        totalAmount: [{ required: true, message: "订单总金额不能为空", trigger: "blur" }],
      },
      snapshotLabelMap: {
        'image': '产品图片',
        'title': '签证名称',
        'unitPrice': '单价'
      },
      addressLabelMap: {
        'contactName': '收件人',
        'contactPhone': '联系电话',
        'address': '详细地址'
      },
    }
  },
  created() {
    this.initDataMapping();
    this.getList();
  },
  methods: {
    /** 新增功能：初始加载相关外表数据填充下拉 */
    initDataMapping() {
      // 拉取客户全量或大分页数据
      listCustomer({ pageSize: 5000 }).then(res => {
        this.customerList = res.rows || [];
      }).catch(() => { });

      // 拉取产品全量
      listProduct({ pageSize: 5000 }).then(res => {
        this.productList = res.rows || [];
      }).catch(() => { });
    },

    /** 解析完整路径，适配 RuoYi 的 /profile 路径拦截 */
    resolveUrl(url) {
      if (!url) return '';
      // 如果已经是绝对路径 http://, https://, 或者 base64 数据, 则直接返回
      if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
        return url;
      }
      // 否则为若依系统的本地上传相对路径模式 (如 /profile/upload/...)
      // 拼接过程环境变量 baseURL，否则会从当前 /visa/order 接口去搜寻文件报404无法加载
      const baseUrl = process.env.VUE_APP_BASE_API;
      return baseUrl + url;
    },

    /** 根据 ID 提取客户名 */
    getCustomerName(id) {
      if (!id) return '-';
      const cust = this.customerList.find(c => c.id == id);
      // 这里的 realName 对应数据库的 real_name
      return cust ? (cust.realName || cust.nickName || cust.userName || id) : id;
    },

    /** 新增功能：根据 ID 提取产品名 */
    getProductName(id) {
      if (!id) return '-';
      const prod = this.productList.find(p => p.id == id);
      return prod ? (prod.title || prod.productName || prod.name || id) : id;
    },

    /** 新增功能：判定是否是图片或URL */
    isImage(url) {
      if (!url) return false;
      const lowerUrl = url.toLowerCase();
      return lowerUrl.endsWith('.png') || lowerUrl.endsWith('.jpg') || lowerUrl.endsWith('.jpeg') || lowerUrl.endsWith('.gif') || lowerUrl.endsWith('.bmp') || lowerUrl.endsWith('.webp');
    },
    isValidUrl(string) {
      try { return Boolean(new URL(string)); }
      catch (e) { return string.startsWith('http') || string.startsWith('/'); }
    },

    /** 新增功能：9 级状态解析名称 */
    getStatusName(status) {
      const map = {
        0: '待支付', 1: '待上传材料', 2: '材料审核中', 3: '材料补交中',
        4: '办理中/签证被Check', 5: '待收货', 6: '已完成',
        7: '材料过审，待预约面试', 8: '待面试', 9: '待寄送原件'
      };
      return map[status] || status;
    },
    /** 新增功能：9 级状态解析颜色 */
    getStatusTagType(status) {
      const map = {
        0: 'info',      // 待支付 (灰)
        1: 'warning',   // 待上传 (黄)
        2: 'warning',   // 审核中 (黄)
        3: 'danger',    // 补交中 (红)
        4: 'warning',   // 办理中/Check (黄)
        5: 'primary',   // 待收货 (蓝)
        6: 'success',   // 已完成 (绿)
        7: 'primary',   // 待预约面试 (蓝)
        8: 'primary',   // 待面试 (蓝)
        9: 'primary'    // 待寄送原件 (蓝)
      };
      return map[status] || 'info';
    },

    /** 面试反馈状态映射 */
    getInterviewFeedbackName(val) {
      const map = { '0': '未反馈', '1': '过签', '2': '拒签', '3': 'Check' };
      return map[val] || val;
    },
    getInterviewFeedbackType(val) {
      const map = { '0': 'info', '1': 'success', '2': 'danger', '3': 'warning' };
      return map[val] || 'info';
    },

    /** 解码 JSON 并在弹窗预览（对象类：快照/地址等） */
    handleViewSnapshot(jsonStr, title) {
      this.snapshotTitle = title || '详情';
      try {
        if (jsonStr) {
          this.currentSnapshotObj = JSON.parse(jsonStr);
        } else {
          this.currentSnapshotObj = {};
        }
      } catch (e) {
        // 如果不是标准JSON，则放到一个默认项里
        this.currentSnapshotObj = { '源数据': jsonStr };
      }
      this.snapshotOpen = true;
    },

    /** 解码 JSON 并在弹窗预览（支持多人材料结构） */
    handleViewMaterials(jsonStr, row) {
      try {
        const parsed = JSON.parse(jsonStr);
        const result = [];

        if (Array.isArray(parsed)) {
          // 遍历每一位申请人的材料包
          parsed.forEach((materialsObj, index) => {
            // 尝试从 row.applicantList 中匹配真实姓名，匹配不到则显示“申请人 n”
            const applicantName = (row.applicantList && row.applicantList[index])
              ? row.applicantList[index].name
              : `申请人 ${index + 1}`;

            // 遍历该申请人名下的所有键值对（材料项）
            Object.keys(materialsObj).forEach(key => {
              result.push({
                applicantName: applicantName, // 申请人姓名
                materialKey: key,            // 材料名称（如：护照首页）
                materialValue: materialsObj[key] // 材料内容（路径或文本）
              });
            });
          });
          this.currentMaterialsArray = result;
        } else {
          this.currentMaterialsArray = [];
        }
      } catch (e) {
        this.currentMaterialsArray = jsonStr ? [{ applicantName: '-', materialKey: '原始数据', materialValue: jsonStr }] : [];
      }
      this.materialsOpen = true;
    },

    hasAuditFeedback(jsonStr) {
      if (!jsonStr || jsonStr === '[]') return false;
      try {
        const arr = JSON.parse(jsonStr);
        if (Array.isArray(arr)) {
          // 只要有一个人的 text 字段不为空，就返回 true
          return arr.some(item => item.text && item.text.trim() !== '');
        }
        return false;
      } catch (e) {
        // 如果是老版本的纯文本格式，直接判断
        return !!jsonStr && jsonStr !== '[]';
      }
    },

    /** 查看审核备注记录 */
    handleViewAuditRecords(row) {
      const jsonStr = row.auditRemark;
      const applicants = row.applicantList || [];
      const result = [];

      try {
        const parsed = JSON.parse(jsonStr);
        if (Array.isArray(parsed)) {
          parsed.forEach((item, index) => {
            // 只收集有文字内容的反馈
            if (item.text && item.text.trim() !== '') {
              result.push({
                name: applicants[index] ? applicants[index].name : `申请人 ${index + 1}`,
                text: item.text
              });
            }
          });
          this.currentAuditRecords = result;
        } else {
          this.currentAuditRecords = [];
        }
      } catch (e) {
        // 兼容处理老数据
        this.currentAuditRecords = jsonStr ? [{ name: '总体反馈', text: jsonStr }] : [];
      }
      this.auditOpen = true;
    },

    /** 获取订单中的第一位申请人姓名用于列表展示 */
    getFirstApplicantName(row) {
      // 逻辑：如果有关联的申请人列表且长度大于0，返回第一个人的名字
      if (row.applicantList && row.applicantList.length > 0) {
        // 如果人多，可以显示 "张三 等"
        const name = row.applicantList[0].name;
        return row.applicantList.length > 1 ? `${name} 等` : name;
      }
      // 如果没有申请人信息，返回占位符
      return "未填写";
    },

    /** 判断是否有结果数据 */
    hasVisaResult(jsonStr) {
      return jsonStr && jsonStr !== '[]' && jsonStr !== '';
    },

    /** ★ 核心：格式化结果数据，将姓名和结果 JSON 对应起来 ★ */
    formatVisaResult(row) {
      const results = this.parseJson(row.visaResult);
      const applicants = row.applicantList || [];

      return results.map((res, index) => {
        return {
          name: applicants[index] ? applicants[index].name : `申请人 ${index + 1}`,
          status: res.status,
          fileUrl: res.fileUrl,
          memo: res.memo || '-'
        };
      });
    },

    /** 结果标签类型 */
    getResultTagType(status) {
      const map = { 1: 'success', 2: 'danger', 3: 'warning' };
      return map[status] || 'info';
    },

    /** 结果文字转换 */
    getResultText(status) {
      const map = { 1: '获签', 2: '拒签', 3: 'Check' };
      return map[status] || '处理中';
    },

    /** 判断面试反馈是否有数据 */
    hasInterviewFeedback(jsonStr) {
      return jsonStr && jsonStr !== '[]' && jsonStr !== '';
    },

    /** ★ 核心：格式化面试反馈，对齐申请人姓名 ★ */
    formatInterviewFeedback(row) {
      const feedbacks = this.parseJson(row.interviewFeedback);
      const applicants = row.applicantList || [];

      return feedbacks.map((item, index) => {
        return {
          // 对齐姓名
          name: applicants[index] ? applicants[index].name : `申请人 ${index + 1}`,
          // 注意：C端提交的字段名通常是 result
          result: item.result
        };
      });
    },

    /** 面试反馈标签颜色 */
    getFeedbackTagType(status) {
      const map = { 1: 'success', 2: 'danger', 3: 'warning' };
      return map[status] || 'info';
    },

    /** 面试反馈文字转换 */
    getFeedbackText(status) {
      const map = { 1: '过签', 2: '拒签', 3: '被Check' };
      return map[status] || '未反馈';
    },

    // 确保之前的 parseJson 方法还在：
    parseJson(json) {
      try {
        return JSON.parse(json || '[]');
      } catch (e) {
        return [];
      }
    },

    handleContactCustomer(row) {
      // 获取客户真实姓名（复用你之前的 getCustomerName 方法）
      const name = this.getCustomerName(row.customerId);

      // 跳转到客服中心路由，并携带 客户ID、订单ID、订单号、姓名
      this.$router.push({
        path: '/chat', // ★ 请确保这里的路径和你路由定义的客服中心路径一致
        query: {
          customerId: row.customerId,
          orderId: row.id,
          orderNo: row.orderNo,
          nickname: name
        }
      });
    },



    /** 基本增删改查保持不变 */
    getList() {
      this.loading = true
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: null, orderNo: null, customerId: null, productId: null, quantity: null,
        productSnapshot: null, totalAmount: null, status: null, alipayNo: null,
        payTime: null, submittedMaterials: null, auditRemark: null, interviewTime: null,
        interviewLocation: null, interviewFile: null, interviewFeedback: null,
        visaResult: null, trackingNumber: null, mailingAddress: null, delFlag: null
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加签证订单"
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getOrder(id).then(response => {
        this.form = response.data
        // 如果后端传回的反馈结果是数字，转成String避免select匹配问题
        if (this.form.interviewFeedback != null) this.form.interviewFeedback = String(this.form.interviewFeedback)
        if (this.form.visaResult != null) this.form.visaResult = String(this.form.visaResult)

        this.open = true
        this.title = "修改签证订单"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addOrder(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除该订单？').then(function () {
        return delOrder(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => { })
    },
    handleExport() {
      this.download('visa/order/export', {
        ...this.queryParams
      }, `order_${new Date().getTime()}.xlsx`)
    },

    parseJson(json) {
      try {
        // 关键：如果 json 是空的或者格式不对，返回空数组，防止 v-for 报错
        return JSON.parse(json || '[]');
      } catch (e) {
        return [];
      }
    },
  }
}
</script>

<style scoped>
/* 增加图片槽样式，使得图片破损时更明显 */
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}
</style>
