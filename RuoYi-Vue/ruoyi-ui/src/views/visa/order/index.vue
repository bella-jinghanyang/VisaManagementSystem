<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单编号(唯一)" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号(唯一)" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户ID(关联c_customer)" prop="customerId">
        <el-input v-model="queryParams.customerId" placeholder="请输入客户ID(关联c_customer)" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="产品ID(关联visa_product)" prop="productId">
        <el-input v-model="queryParams.productId" placeholder="请输入产品ID(关联visa_product)" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="订单数量" prop="quantity">
        <el-input v-model="queryParams.quantity" placeholder="请输入订单数量" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="订单总金额" prop="totalAmount">
        <el-input v-model="queryParams.totalAmount" placeholder="请输入订单总金额" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="支付宝交易流水号" prop="alipayNo">
        <el-input v-model="queryParams.alipayNo" placeholder="请输入支付宝交易流水号" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="支付成功时间" prop="payTime">
        <el-date-picker clearable v-model="queryParams.payTime" type="date" value-format="yyyy-MM-dd"
          placeholder="请选择支付成功时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="预约面试时间" prop="interviewTime">
        <el-date-picker clearable v-model="queryParams.interviewTime" type="date" value-format="yyyy-MM-dd"
          placeholder="请选择预约面试时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="面试地点" prop="interviewLocation">
        <el-input v-model="queryParams.interviewLocation" placeholder="请输入面试地点" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="面试反馈(0未反馈 1过签 2拒签 3Check)" prop="interviewFeedback">
        <el-input v-model="queryParams.interviewFeedback" placeholder="请输入面试反馈(0未反馈 1过签 2拒签 3Check)" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="最终签证结果(1出签 2拒签)" prop="visaResult">
        <el-input v-model="queryParams.visaResult" placeholder="请输入最终签证结果(1出签 2拒签)" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="寄回护照的快递单号" prop="trackingNumber">
        <el-input v-model="queryParams.trackingNumber" placeholder="请输入寄回护照的快递单号" clearable
          @keyup.enter.native="handleQuery" />
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
      <el-table-column label="订单ID" align="center" prop="id" />
      <el-table-column label="订单编号(唯一)" align="center" prop="orderNo" />
      <el-table-column label="客户ID(关联c_customer)" align="center" prop="customerId" />
      <el-table-column label="产品ID(关联visa_product)" align="center" prop="productId" />
      <el-table-column label="订单数量" align="center" prop="quantity" />
      <el-table-column label="产品信息快照(存下单时的标题和价格)" align="center" prop="productSnapshot" />
      <el-table-column label="订单总金额" align="center" prop="totalAmount" />
      <el-table-column label="订单状态(0待支付 1待上传 2待审核 3待补交 4办理中 5待收货 6已完成 7待面试)" align="center" prop="status" />
      <el-table-column label="支付宝交易流水号" align="center" prop="alipayNo" />
      <el-table-column label="支付成功时间" align="center" prop="payTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.payTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用户提交的动态材料数据(JSON)" align="center" prop="submittedMaterials" />
      <el-table-column label="审核备注/驳回原因" align="center" prop="auditRemark" />
      <el-table-column label="预约面试时间" align="center" prop="interviewTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.interviewTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="面试地点" align="center" prop="interviewLocation" />
      <el-table-column label="面试预约单文件路径" align="center" prop="interviewFile" />
      <el-table-column label="面试反馈(0未反馈 1过签 2拒签 3Check)" align="center" prop="interviewFeedback" />
      <el-table-column label="最终签证结果(1出签 2拒签)" align="center" prop="visaResult" />
      <el-table-column label="寄回护照的快递单号" align="center" prop="trackingNumber" />
      <el-table-column label="收货地址快照(收件人,电话,地址)" align="center" prop="mailingAddress" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['visa:order:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['visa:order:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改签证订单对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="订单编号(唯一)" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入订单编号(唯一)" />
        </el-form-item>
        <el-form-item label="客户ID(关联c_customer)" prop="customerId">
          <el-input v-model="form.customerId" placeholder="请输入客户ID(关联c_customer)" />
        </el-form-item>
        <el-form-item label="产品ID(关联visa_product)" prop="productId">
          <el-input v-model="form.productId" placeholder="请输入产品ID(关联visa_product)" />
        </el-form-item>
        <el-form-item label="订单数量" prop="quantity">
          <el-input v-model="form.quantity" placeholder="请输入订单数量" />
        </el-form-item>
        <el-form-item label="产品信息快照(存下单时的标题和价格)" prop="productSnapshot">
          <el-input v-model="form.productSnapshot" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="订单总金额" prop="totalAmount">
          <el-input v-model="form.totalAmount" placeholder="请输入订单总金额" />
        </el-form-item>
        <el-form-item label="支付宝交易流水号" prop="alipayNo">
          <el-input v-model="form.alipayNo" placeholder="请输入支付宝交易流水号" />
        </el-form-item>
        <el-form-item label="支付成功时间" prop="payTime">
          <el-date-picker clearable v-model="form.payTime" type="date" value-format="yyyy-MM-dd"
            placeholder="请选择支付成功时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="用户提交的动态材料数据(JSON)" prop="submittedMaterials">
          <el-input v-model="form.submittedMaterials" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="审核备注/驳回原因" prop="auditRemark">
          <el-input v-model="form.auditRemark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="预约面试时间" prop="interviewTime">
          <el-date-picker clearable v-model="form.interviewTime" type="date" value-format="yyyy-MM-dd"
            placeholder="请选择预约面试时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="面试地点" prop="interviewLocation">
          <el-input v-model="form.interviewLocation" placeholder="请输入面试地点" />
        </el-form-item>
        <el-form-item label="面试预约单文件路径" prop="interviewFile">
          <file-upload v-model="form.interviewFile" />
        </el-form-item>
        <el-form-item label="面试反馈(0未反馈 1过签 2拒签 3Check)" prop="interviewFeedback">
          <el-input v-model="form.interviewFeedback" placeholder="请输入面试反馈(0未反馈 1过签 2拒签 3Check)" />
        </el-form-item>
        <el-form-item label="最终签证结果(1出签 2拒签)" prop="visaResult">
          <el-input v-model="form.visaResult" placeholder="请输入最终签证结果(1出签 2拒签)" />
        </el-form-item>
        <el-form-item label="寄回护照的快递单号" prop="trackingNumber">
          <el-input v-model="form.trackingNumber" placeholder="请输入寄回护照的快递单号" />
        </el-form-item>
        <el-form-item label="收货地址快照(收件人,电话,地址)" prop="mailingAddress">
          <el-input v-model="form.mailingAddress" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="删除标志(0代表存在 2代表删除)" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="请输入删除标志(0代表存在 2代表删除)" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
        <el-button size="mini" type="text" icon="el-icon-chat-dot-round"
          @click="handleContactCustomer(scope.row)">联系客户</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addOrder, delOrder, getOrder, listOrder, updateOrder } from "@/api/visa/order";

export default {
  name: "Order",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 签证订单表格数据
      orderList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        customerId: null,
        productId: null,
        quantity: null,
        productSnapshot: null,
        totalAmount: null,
        status: null,
        alipayNo: null,
        payTime: null,
        submittedMaterials: null,
        auditRemark: null,
        interviewTime: null,
        interviewLocation: null,
        interviewFile: null,
        interviewFeedback: null,
        visaResult: null,
        trackingNumber: null,
        mailingAddress: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        orderNo: [
          { required: true, message: "订单编号(唯一)不能为空", trigger: "blur" }
        ],
        customerId: [
          { required: true, message: "客户ID(关联c_customer)不能为空", trigger: "blur" }
        ],
        productId: [
          { required: true, message: "产品ID(关联visa_product)不能为空", trigger: "blur" }
        ],
        totalAmount: [
          { required: true, message: "订单总金额不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询签证订单列表 */
    getList() {
      this.loading = true
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        orderNo: null,
        customerId: null,
        productId: null,
        quantity: null,
        productSnapshot: null,
        totalAmount: null,
        status: null,
        alipayNo: null,
        payTime: null,
        submittedMaterials: null,
        auditRemark: null,
        interviewTime: null,
        interviewLocation: null,
        interviewFile: null,
        interviewFeedback: null,
        visaResult: null,
        trackingNumber: null,
        mailingAddress: null,
        createTime: null,
        updateTime: null,
        delFlag: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加签证订单"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getOrder(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改签证订单"
      })
    },
    /** 提交按钮 */
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
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除签证订单编号为"' + ids + '"的数据项？').then(function () {
        return delOrder(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => { })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('visa/order/export', {
        ...this.queryParams
      }, `order_${new Date().getTime()}.xlsx`)
    },
    handleContactCustomer(row) {
      // 跳转到客服中心，并带上参数
      this.$router.push({
        path: '/visa/chat',
        query: {
          customerId: row.customerId,
          orderId: row.id,
          orderNo: row.orderNo
        }
      });
    }
  }
}
</script>
