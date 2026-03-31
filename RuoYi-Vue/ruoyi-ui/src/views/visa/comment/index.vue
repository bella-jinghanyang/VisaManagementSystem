<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      
      <el-form-item label="产品名称" prop="productId">
        <el-select v-model="queryParams.productId" placeholder="全部产品" clearable filterable @change="handleQuery">
          <el-option v-for="item in productList" :key="item.id" :label="item.title" :value="item.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="客户名称" prop="customerId">
        <el-select v-model="queryParams.customerId" placeholder="全部客户" clearable filterable @change="handleQuery">
          <el-option v-for="item in customerList" :key="item.id" :label="item.realName || item.nickName" :value="item.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="评分" prop="rating">
        <el-select v-model="queryParams.rating" placeholder="选择评分" clearable @change="handleQuery">
          <el-option v-for="i in 5" :key="i" :label="i + ' 星'" :value="i" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['visa:comment:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['visa:comment:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="commentList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="id" width="60" />
      
      <!-- ID 转名称展示 -->
      <el-table-column label="订单编号" align="center" prop="orderNo" width="180" />
      <el-table-column label="签证产品" align="center" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ getProductName(scope.row.productId) }}
        </template>
      </el-table-column>
      <el-table-column label="评价客户" align="center" width="120">
        <template slot-scope="scope">
          {{ getCustomerName(scope.row.customerId) }}
        </template>
      </el-table-column>

      <!-- 评分 UI 优化 -->
      <el-table-column label="评分" align="center" width="160">
        <template slot-scope="scope">
          <!-- 1. 增加 :key 确保每次数据变动都重新渲染 -->
          <!-- 2. 强制转换 Number -->
          <el-rate :key="scope.row.id" :value="Number(scope.row.rating)" disabled show-score score-template="{value}"
            text-color="#ff9900">
          </el-rate>
        </template>
      </el-table-column>

      <el-table-column label="评价内容" prop="content" min-width="200" show-overflow-tooltip />
      
      <el-table-column label="回复内容" align="center" prop="adminReply" show-overflow-tooltip>
        <template slot-scope="scope">
          <span v-if="scope.row.adminReply" style="color: #67C23A">{{ scope.row.adminReply }}</span>
          <span v-else class="text-muted">暂未回复</span>
        </template>
      </el-table-column>

      <el-table-column label="回复时间" align="center" prop="replyTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.replyTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-chat-dot-round" @click="handleReply(scope.row)" v-hasPermi="['visa:comment:edit']">回复</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['visa:comment:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 管理员回复对话框 -->
    <el-dialog title="回复用户评价" :visible.sync="replyOpen" width="500px" append-to-body>
      <div v-if="currentComment.id" style="padding: 10px; background: #f8f9fa; border-radius: 8px; margin-bottom: 20px;">
        <div style="margin-bottom: 10px;"><strong>客户：</strong>{{ getCustomerName(currentComment.customerId) }}</div>
        <div style="margin-bottom: 10px;"><strong>评分：</strong><el-rate v-model="currentComment.rating" disabled style="display:inline-block; vertical-align:middle" /></div>
        <div style="line-height: 1.6;"><strong>评价：</strong>{{ currentComment.content }}</div>
      </div>
      <el-form label-width="80px">
        <el-form-item label="官方回复">
          <el-input type="textarea" v-model="replyText" :rows="5" placeholder="请输入您的回复内容，这会让客户感到被重视..." />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReply">发布回复</el-button>
        <el-button @click="replyOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listComment, delComment, updateComment } from "@/api/visa/comment";
// 引入关联数据 API
import { listCustomer } from "@/api/visa/customer";
import { listProduct } from "@/api/visa/product";

export default {
  name: "Comment",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      commentList: [],
      customerList: [],
      productList: [],
      open: false,
      replyOpen: false,
      replyText: "",
      currentComment: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        productId: null,
        customerId: null,
        rating: null,
      }
    }
  },
  created() {
    this.initBaseData();
    this.getList();
  },
  methods: {
    /** 初始加载关联数据 */
    initBaseData() {
      listCustomer({ pageSize: 1000 }).then(res => { this.customerList = res.rows; });
      listProduct({ pageSize: 1000 }).then(res => { this.productList = res.rows; });
    },
    /** 获取客户名 */
    getCustomerName(id) {
      const item = this.customerList.find(c => c.id == id);
      return item ? (item.realName || item.nickName || id) : id;
    },
    /** 获取产品名 */
    getProductName(id) {
      const item = this.productList.find(p => p.id == id);
      return item ? (item.title || id) : id;
    },
    /** 查询评价列表 */
    getList() {
      this.loading = true;
      listComment(this.queryParams).then(response => {
        this.commentList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.multiple = !selection.length;
    },
    /** 回复操作 */
    handleReply(row) {
      this.currentComment = JSON.parse(JSON.stringify(row));
      this.replyText = row.adminReply || "";
      this.replyOpen = true;
    },
    /** 提交回复 */
    submitReply() {
      if (!this.replyText.trim()) return this.$message.warning("请输入回复内容");
      const data = {
        id: this.currentComment.id,
        adminReply: this.replyText,
        replyTime: new Date()
      };
      updateComment(data).then(res => {
        this.$modal.msgSuccess("回复成功");
        this.replyOpen = false;
        this.getList();
      });
    },
    /** 删除操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除评价记录？').then(function () {
        return delComment(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
    handleExport() {
      this.download('visa/comment/export', { ...this.queryParams }, `comment_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.text-muted { color: #909399; font-size: 13px; }
</style>