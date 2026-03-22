<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="关联订单ID" prop="orderId">
        <el-input v-model="queryParams.orderId" placeholder="请输入关联订单ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="关联产品ID" prop="productId">
        <el-input v-model="queryParams.productId" placeholder="请输入关联产品ID" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="关联客户ID" prop="customerId">
        <el-input v-model="queryParams.customerId" placeholder="请输入关联客户ID" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="评分" prop="rating">
        <el-input v-model="queryParams.rating" placeholder="请输入评分" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="回复时间" prop="replyTime">
        <el-date-picker clearable v-model="queryParams.replyTime" type="date" value-format="yyyy-MM-dd"
          placeholder="请选择回复时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['visa:comment:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['visa:comment:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['visa:comment:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['visa:comment:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="commentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="评价ID" align="center" prop="id" />
      <el-table-column label="关联订单ID" align="center" prop="orderId" />
      <el-table-column label="关联产品ID" align="center" prop="productId" />
      <el-table-column label="关联客户ID" align="center" prop="customerId" />
      <el-table-column label="评分" align="center" prop="rating" />
      <el-table-column label="评价内容" align="center" prop="content" />
      <el-table-column label="晒图" align="center" prop="images" />
      <el-table-column label="管理员回复" align="center" prop="adminReply" />
      <el-table-column label="回复时间" align="center" prop="replyTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.replyTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-chat-dot-round" @click="handleReply(scope.row)"
            v-hasPermi="['visa:comment:edit']">回复</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['visa:comment:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['visa:comment:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改签证评价对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="关联订单ID" prop="orderId">
          <el-input v-model="form.orderId" placeholder="请输入关联订单ID" />
        </el-form-item>
        <el-form-item label="关联产品ID" prop="productId">
          <el-input v-model="form.productId" placeholder="请输入关联产品ID" />
        </el-form-item>
        <el-form-item label="关联客户ID" prop="customerId">
          <el-input v-model="form.customerId" placeholder="请输入关联客户ID" />
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-input v-model="form.rating" placeholder="请输入评分" />
        </el-form-item>
        <el-form-item label="评价内容">
          <editor v-model="form.content" :min-height="192" />
        </el-form-item>
        <el-form-item label="管理员回复" prop="adminReply">
          <el-input v-model="form.adminReply" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="回复时间" prop="replyTime">
          <el-date-picker clearable v-model="form.replyTime" type="date" value-format="yyyy-MM-dd"
            placeholder="请选择回复时间">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 管理员回复对话框 -->
    <el-dialog title="回复用户评价" :visible.sync="replyOpen" width="500px" append-to-body>
      <div style="margin-bottom: 20px; color: #606266; background: #f4f4f5; padding: 15px; border-radius: 8px;">
        <strong>用户评价：</strong> {{ currentComment.content }}
      </div>
      <el-form label-width="80px">
        <el-form-item label="回复内容">
          <el-input type="textarea" v-model="replyText" :rows="4" placeholder="请输入官方回复内容..." />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReply">确 定</el-button>
        <el-button @click="replyOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listComment, getComment, delComment, addComment, updateComment } from "@/api/visa/comment"

export default {
  name: "Comment",
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
      // 签证评价表格数据
      commentList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderId: null,
        productId: null,
        customerId: null,
        rating: null,
        content: null,
        images: null,
        adminReply: null,
        replyTime: null,
      },
      // 表单参数
      form: {},
      replyOpen: false,
      replyText: "",
      currentComment: {},
      // 表单校验
      rules: {
        orderId: [
          { required: true, message: "关联订单ID不能为空", trigger: "blur" }
        ],
        productId: [
          { required: true, message: "关联产品ID不能为空", trigger: "blur" }
        ],
        customerId: [
          { required: true, message: "关联客户ID不能为空", trigger: "blur" }
        ],
        content: [
          { required: true, message: "评价内容不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询签证评价列表 */
    getList() {
      this.loading = true
      listComment(this.queryParams).then(response => {
        this.commentList = response.rows
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
        productId: null,
        customerId: null,
        rating: null,
        content: null,
        images: null,
        adminReply: null,
        replyTime: null,
        createTime: null
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
      this.title = "添加签证评价"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getComment(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改签证评价"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateComment(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addComment(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除签证评价编号为"' + ids + '"的数据项？').then(function () {
        return delComment(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => { })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('visa/comment/export', {
        ...this.queryParams
      }, `comment_${new Date().getTime()}.xlsx`)
    },
    /** ★★★ 打开回复弹窗 ★★★ */
    handleReply(row) {
      this.currentComment = row;
      this.replyText = row.adminReply || ""; // 如果之前回过，回显出来
      this.replyOpen = true;
    },

    /** ★★★ 提交回复 ★★★ */
    submitReply() {
      if (!this.replyText.trim()) return this.$message.warning("请输入回复内容");

      // 组装更新数据
      const data = {
        id: this.currentComment.id,
        adminReply: this.replyText,
        replyTime: new Date() // 这里前端传时间，也可以后端在接口里补
      };

      updateComment(data).then(res => {
        this.$modal.msgSuccess("回复成功");
        this.replyOpen = false;
        this.getList(); // 刷新列表
      });
    }
  }
}
</script>
