<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <!-- 1. 搜索优化：用订单编号代替订单ID -->
      <el-form-item label="订单编号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      
      <el-form-item label="发送者类型" prop="senderType">
        <el-select v-model="queryParams.senderType" placeholder="请选择发送者" clearable @change="handleQuery">
          <el-option label="客户" :value="1" />
          <el-option label="管理员" :value="2" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <!-- 删除了新增按钮，通常留言记录由业务触发产生，不手动新增 -->
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['visa:message:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['visa:message:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="messageList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="编号" align="center" prop="id" width="80" />
      
      <!-- 2. ID转名称：显示订单编号 -->
      <el-table-column label="关联订单编号" align="center" prop="orderNo" min-width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.orderNo || '通用咨询' }}</span>
        </template>
      </el-table-column>

      <!-- 3. 数字转文字：发送者身份标签化 -->
      <el-table-column label="发送者身份" align="center" prop="senderType" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.senderType === 2 ? 'primary' : 'success'">
            {{ getSenderLabel(scope.row.senderType) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="消息内容" align="left" prop="content" show-overflow-tooltip />
      
      <el-table-column label="发送时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <!-- 删除了图片路径列 -->

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['visa:message:remove']"
          >删除记录</el-button>
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

    <!-- 修改对话框：同步删除图片路径字段 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联订单">
          <el-input :value="form.orderNo || '通用咨询'" disabled />
        </el-form-item>
        <el-form-item label="发送者">
          <el-tag>{{ getSenderLabel(form.senderType) }}</el-tag>
        </el-form-item>
        <el-form-item label="消息内容">
          <!-- 考虑到记录不可篡改性，此处通常设为只读或简单的预览 -->
          <div v-html="form.content" style="padding: 10px; background: #f8f9fa; border-radius: 4px; border: 1px solid #ddd;"></div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMessage, getMessage, delMessage } from "@/api/visa/message"

export default {
  name: "Message",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      messageList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null, // 搜索字段改为 orderNo
        senderType: null,
      },
      form: {},
      rules: {}
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true
      listMessage(this.queryParams).then(response => {
        this.messageList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 发送者身份转换 */
    getSenderLabel(type) {
      const map = {
        1: '客户',
        2: '管理员'
      };
      return map[type] || '未知';
    },
    /** 搜索 */
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
      this.single = selection.length!==1;
      this.multiple = !selection.length;
    },
    /** 查看详情逻辑 */
    handleUpdate(row) {
      const id = row.id || this.ids;
      getMessage(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "查看留言详情";
      });
    },
    /** 删除 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除该条留言记录？').then(function() {
        return delMessage(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('visa/message/export', {
        ...this.queryParams
      }, `message_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>