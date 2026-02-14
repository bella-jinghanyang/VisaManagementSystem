<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="国家名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入国家名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="国家代码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入国家代码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="所属大洲" prop="continent">
        <el-input v-model="queryParams.continent" placeholder="请输入所属大洲" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="国旗图片" prop="flagUrl">
        <el-input v-model="queryParams.flagUrl" placeholder="请输入国旗图片" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input v-model="queryParams.sort" placeholder="请输入排序" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['system:country:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['system:country:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['system:country:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['system:country:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="countryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="国家名称" align="center" prop="name" />
      <el-table-column label="国家代码" align="center" prop="code" />
      <el-table-column label="所属大洲" align="center" prop="continent" />
      <el-table-column label="国旗图片" align="center" prop="flagUrl">
        <template slot-scope="scope">
          <image-preview :src="scope.row.flagUrl" :width="50" :height="50" />
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" />
      <el-table-column label="上架状态" align="center" prop="status">
        <template slot-scope=" scope">
          <el-switch v-model="scope.row.status" active-value="1" inactive-value="0" active-text="上架" inactive-text="下架"
            @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['system:country:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['system:country:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改国家配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="国家名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入国家名称" />
        </el-form-item>
        <el-form-item label="国家代码" prop="code">
          <el-input v-model="form.code" placeholder="请输入国家代码" />
        </el-form-item>
        <el-form-item label="所属大洲" prop="continent">
          <el-input v-model="form.continent" placeholder="请输入所属大洲" />
        </el-form-item>
        <el-form-item label="国旗图片" prop="flagUrl">
          <image-upload v-model="form.flagUrl" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input v-model="form.sort" placeholder="请输入排序" />
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
import { listCountry, getCountry, delCountry, addCountry, updateCountry, changeCountryStatus } from "@/api/visa/country"

export default {
  name: "Country",
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
      // 国家配置表格数据
      countryList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        code: null,
        continent: null,
        flagUrl: null,
        sort: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "国家名称(如:日本)不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询国家配置列表 */
    getList() {
      this.loading = true
      listCountry(this.queryParams).then(response => {
        this.countryList = response.rows
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
        name: null,
        code: null,
        continent: null,
        flagUrl: null,
        sort: null,
        status: null
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
      this.title = "添加国家配置"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getCountry(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改国家配置"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateCountry(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addCountry(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除国家配置编号为"' + ids + '"的数据项？').then(function () {
        return delCountry(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => { })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/country/export', {
        ...this.queryParams
      }, `country_${new Date().getTime()}.xlsx`)
    },
    /** 上架/下架开关 */
    handleStatusChange(row) {
      const oldStatus = row.status === "1" ? "0" : "1"; // 因为 switch 已经切完了，所以 old 是反过来的
      const text = row.status === "1" ? "上架" : "下架";

      this.$modal.confirm(`确认要${text}【${row.name}】吗？`)
        .then(() => {
          return changeCountryStatus(row.id, row.status);
        })
        .then(() => {
          this.$modal.msgSuccess(`${text}成功`);
        })
        .catch(() => {
          // 回滚
          row.status = oldStatus;
        });

    }
  }
}
</script>
