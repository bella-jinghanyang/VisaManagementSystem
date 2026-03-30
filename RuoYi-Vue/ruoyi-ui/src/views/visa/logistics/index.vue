<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="关联订单ID" prop="orderId">
        <el-input
          v-model="queryParams.orderId"
          placeholder="请输入关联订单ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入关联订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="方向: 1-用户寄给中介, 2-中介寄回用户" prop="direction">
        <el-input
          v-model="queryParams.direction"
          placeholder="请输入方向: 1-用户寄给中介, 2-中介寄回用户"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="快递公司(顺丰/EMS等)" prop="courierCompany">
        <el-input
          v-model="queryParams.courierCompany"
          placeholder="请输入快递公司(顺丰/EMS等)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="快递单号" prop="trackingNo">
        <el-input
          v-model="queryParams.trackingNo"
          placeholder="请输入快递单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="寄件人姓名" prop="senderName">
        <el-input
          v-model="queryParams.senderName"
          placeholder="请输入寄件人姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="寄件人电话" prop="senderPhone">
        <el-input
          v-model="queryParams.senderPhone"
          placeholder="请输入寄件人电话"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="签收时间" prop="receiveTime">
        <el-date-picker clearable
          v-model="queryParams.receiveTime"
          type="date"
          value-format="yyyy-MM-dd"
          placeholder="请选择签收时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['visa:logistics:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['visa:logistics:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['visa:logistics:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['visa:logistics:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logisticsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="${comment}" align="center" prop="id" />
      <el-table-column label="关联订单ID" align="center" prop="orderId" />
      <el-table-column label="关联订单号" align="center" prop="orderNo" />
      <el-table-column label="方向: 1-用户寄给中介, 2-中介寄回用户" align="center" prop="direction" />
      <el-table-column label="快递公司(顺丰/EMS等)" align="center" prop="courierCompany" />
      <el-table-column label="快递单号" align="center" prop="trackingNo" />
      <el-table-column label="寄件人姓名" align="center" prop="senderName" />
      <el-table-column label="寄件人电话" align="center" prop="senderPhone" />
      <el-table-column label="详细邮寄地址快照" align="center" prop="mailAddress" />
      <el-table-column label="状态: 0-待寄出, 1-已寄出, 2-已签收" align="center" prop="status" />
      <el-table-column label="签收时间" align="center" prop="receiveTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.receiveTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['visa:logistics:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['visa:logistics:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改订单物流信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="关联订单ID" prop="orderId">
          <el-input v-model="form.orderId" placeholder="请输入关联订单ID" />
        </el-form-item>
        <el-form-item label="关联订单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入关联订单号" />
        </el-form-item>
        <el-form-item label="方向: 1-用户寄给中介, 2-中介寄回用户" prop="direction">
          <el-input v-model="form.direction" placeholder="请输入方向: 1-用户寄给中介, 2-中介寄回用户" />
        </el-form-item>
        <el-form-item label="快递公司(顺丰/EMS等)" prop="courierCompany">
          <el-input v-model="form.courierCompany" placeholder="请输入快递公司(顺丰/EMS等)" />
        </el-form-item>
        <el-form-item label="快递单号" prop="trackingNo">
          <el-input v-model="form.trackingNo" placeholder="请输入快递单号" />
        </el-form-item>
        <el-form-item label="寄件人姓名" prop="senderName">
          <el-input v-model="form.senderName" placeholder="请输入寄件人姓名" />
        </el-form-item>
        <el-form-item label="寄件人电话" prop="senderPhone">
          <el-input v-model="form.senderPhone" placeholder="请输入寄件人电话" />
        </el-form-item>
        <el-form-item label="详细邮寄地址快照" prop="mailAddress">
          <el-input v-model="form.mailAddress" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="签收时间" prop="receiveTime">
          <el-date-picker clearable
            v-model="form.receiveTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择签收时间">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLogistics, getLogistics, delLogistics, addLogistics, updateLogistics } from "@/api/visa/logistics"

export default {
  name: "Logistics",
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
      // 订单物流信息表格数据
      logisticsList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderId: null,
        orderNo: null,
        direction: null,
        courierCompany: null,
        trackingNo: null,
        senderName: null,
        senderPhone: null,
        mailAddress: null,
        status: null,
        receiveTime: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        orderId: [
          { required: true, message: "关联订单ID不能为空", trigger: "blur" }
        ],
        orderNo: [
          { required: true, message: "关联订单号不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询订单物流信息列表 */
    getList() {
      this.loading = true
      listLogistics(this.queryParams).then(response => {
        this.logisticsList = response.rows
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
        orderId: null,
        orderNo: null,
        direction: null,
        courierCompany: null,
        trackingNo: null,
        senderName: null,
        senderPhone: null,
        mailAddress: null,
        status: null,
        createTime: null,
        receiveTime: null
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
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加订单物流信息"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getLogistics(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改订单物流信息"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateLogistics(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addLogistics(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除订单物流信息编号为"' + ids + '"的数据项？').then(function() {
        return delLogistics(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('visa/logistics/export', {
        ...this.queryParams
      }, `logistics_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
