<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="关联订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      
      <!-- 优化：方向搜索改为下拉框 -->
      <el-form-item label="物流方向" prop="direction">
        <el-select v-model="queryParams.direction" placeholder="请选择方向" clearable @change="handleQuery">
          <el-option label="用户寄给中介" :value="1" />
          <el-option label="中介寄回用户" :value="2" />
        </el-select>
      </el-form-item>

      <el-form-item label="快递公司" prop="courierCompany">
        <el-input v-model="queryParams.courierCompany" placeholder="请输入快递公司" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>

      <el-form-item label="快递单号" prop="trackingNo">
        <el-input v-model="queryParams.trackingNo" placeholder="请输入快递单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>

      <!-- 优化：状态搜索改为下拉框 -->
      <el-form-item label="物流状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable @change="handleQuery">
          <el-option label="待寄出" :value="0" />
          <el-option label="已寄出" :value="1" />
          <el-option label="已签收" :value="2" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['visa:logistics:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['visa:logistics:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logisticsList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="订单号" align="center" prop="orderNo" width="160" />
      
      <!-- 数字转文字：物流方向 -->
      <el-table-column label="物流方向" align="center" prop="direction" width="130">
        <template slot-scope="scope">
          <el-tag :type="scope.row.direction === 1 ? 'info' : 'primary'" effect="plain">
            {{ scope.row.direction === 1 ? '用户 ➔ 中介' : '中介 ➔ 用户' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="快递信息" align="left" min-width="150">
        <template slot-scope="scope">
          <div><b>公司：</b>{{ scope.row.courierCompany || '-' }}</div>
          <div><b>单号：</b>{{ scope.row.trackingNo || '-' }}</div>
        </template>
      </el-table-column>

      <el-table-column label="联系人信息" align="left" min-width="150">
        <template slot-scope="scope">
          <div><b>姓名：</b>{{ scope.row.senderName || '-' }}</div>
          <div><b>电话：</b>{{ scope.row.senderPhone || '-' }}</div>
        </template>
      </el-table-column>

      <el-table-column label="详细地址" align="center" prop="mailAddress" show-overflow-tooltip />

      <!-- 数字转文字：物流状态 -->
      <el-table-column label="当前状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusTag(scope.row.status)">
            {{ getStatusName(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="时间轨迹" align="left" width="180">
        <template slot-scope="scope">
          <div style="font-size: 12px;"><b>创建：</b>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</div>
          <div style="font-size: 12px; color: #67C23A;" v-if="scope.row.receiveTime">
            <b>签收：</b>{{ parseTime(scope.row.receiveTime, '{y}-{m}-{d} {h}:{i}') }}
          </div>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['visa:logistics:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['visa:logistics:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改订单物流信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="订单ID" prop="orderId">
              <el-input v-model="form.orderId" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单号" prop="orderNo">
              <el-input v-model="form.orderNo" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="物流方向" prop="direction">
          <el-radio-group v-model="form.direction">
            <el-radio :label="1">用户寄给中介</el-radio>
            <el-radio :label="2">中介寄回用户</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="快递公司" prop="courierCompany">
              <el-input v-model="form.courierCompany" placeholder="请输入公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="快递单号" prop="trackingNo">
              <el-input v-model="form.trackingNo" placeholder="请输入单号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="联系姓名" prop="senderName">
              <el-input v-model="form.senderName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="senderPhone">
              <el-input v-model="form.senderPhone" placeholder="请输入电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址" prop="mailAddress">
          <el-input v-model="form.mailAddress" type="textarea" :rows="3" placeholder="请输入完整地址" />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="当前状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="待寄出" :value="0" />
                <el-option label="已寄出" :value="1" />
                <el-option label="已签收" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签收时间" prop="receiveTime">
              <el-date-picker clearable v-model="form.receiveTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="选择签收时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">保 存</el-button>
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
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      logisticsList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        direction: null,
        courierCompany: null,
        trackingNo: null,
        status: null,
      },
      form: {},
      rules: {
        orderNo: [{ required: true, message: "订单号不能为空", trigger: "blur" }],
        trackingNo: [{ required: true, message: "单号不能为空", trigger: "blur" }],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 状态名称映射 */
    getStatusName(status) {
      const map = { 0: "待寄出", 1: "已寄出", 2: "已签收" };
      return map[status] || "未知";
    },
    /** 状态标签颜色 */
    getStatusTag(status) {
      const map = { 0: "info", 1: "warning", 2: "success" };
      return map[status] || "";
    },
    getList() {
      this.loading = true
      listLogistics(this.queryParams).then(response => {
        this.logisticsList = response.rows
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
        id: null, orderId: null, orderNo: null, direction: 1,
        courierCompany: null, trackingNo: null, senderName: null,
        senderPhone: null, mailAddress: null, status: 0,
        createTime: null, receiveTime: null
      };
      this.resetForm("form");
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
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "手动录入物流"
    },
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getLogistics(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改物流记录"
      })
    },
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
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除选中的物流记录？').then(function() {
        return delLogistics(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('visa/logistics/export', { ...this.queryParams }, `logistics_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>