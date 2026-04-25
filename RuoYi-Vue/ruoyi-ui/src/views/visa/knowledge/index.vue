<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="分类" prop="category">
        <el-input
          v-model="queryParams.category"
          placeholder="请输入分类"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="知识点标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入知识点标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
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
          v-hasPermi="['visa:knowledge:add']"
        >新增文本</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-upload2"
          size="mini"
          @click="handleUploadDoc"
          v-hasPermi="['visa:knowledge:add']"
        >上传文档</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['visa:knowledge:edit']"
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
          v-hasPermi="['visa:knowledge:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['visa:knowledge:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-refresh"
          size="mini"
          :loading="refreshing"
          @click="handleRefreshEmbeddings"
          v-hasPermi="['visa:knowledge:edit']"
        >刷新 ES 向量索引</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="knowledgeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="分类" align="center" prop="category" />
      <el-table-column label="知识点标题" align="center" prop="title" />
      <el-table-column label="搜索关键词" align="center" prop="keywords" />
      <el-table-column label="详细内容" align="center" prop="content" show-overflow-tooltip />
      <el-table-column label="原始文档" align="center" prop="sourceFile" show-overflow-tooltip>
        <template slot-scope="scope">
          <span v-if="scope.row.sourceFile">{{ scope.row.sourceFile }}</span>
          <span v-else style="color:#999">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.status" :active-value="0" :inactive-value="1" active-color="#13ce66"
            inactive-color="#ff4949" @change="handleStatusChange(scope.row)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['visa:knowledge:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['visa:knowledge:remove']"
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

    <!-- 添加或修改签证知识库对话框（文本模式） -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入分类，如：美国/日本/通用" />
        </el-form-item>
        <el-form-item label="知识点标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入知识点标题" />
        </el-form-item>
        <el-form-item label="搜索关键词" prop="keywords">
          <el-input v-model="form.keywords" type="textarea" placeholder="请输入关键词，逗号分隔" />
        </el-form-item>
        <el-form-item label="详细内容" prop="content">
          <editor v-model="form.content" :min-height="192"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="0" :inactive-value="1" active-text="正常"
            inactive-text="停用"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 上传文档对话框（PDF / Word / Excel 等） -->
    <el-dialog title="上传知识文档" :visible.sync="uploadOpen" width="560px" append-to-body>
      <el-form ref="uploadForm" :model="uploadForm" :rules="uploadRules" label-width="90px">
        <el-form-item label="知识标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入知识点标题（必填）" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="uploadForm.category" placeholder="如：美国/日本/通用（选填）" />
        </el-form-item>
        <el-form-item label="搜索关键词" prop="keywords">
          <el-input v-model="uploadForm.keywords" type="textarea" placeholder="逗号分隔（选填）" />
        </el-form-item>
        <el-form-item label="上传文档" prop="file">
          <el-upload
            ref="upload"
            action="#"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.html"
          >
            <el-button size="small" type="primary" icon="el-icon-upload2">选择文件</el-button>
            <div slot="tip" class="el-upload__tip" style="color:#999;font-size:12px;margin-top:4px">
              支持 PDF / Word / Excel / PPT / TXT，文件大小不超过 10MB
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="uploading" @click="submitUpload">开始摄取</el-button>
        <el-button @click="cancelUpload">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listKnowledge, getKnowledge, delKnowledge, addKnowledge, updateKnowledge, uploadKnowledgeDoc, refreshEmbeddings } from "@/api/visa/knowledge"

export default {
  name: "Knowledge",
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
      // 签证知识库表格数据
      knowledgeList: [],
      // 弹出层标题
      title: "",
      // 是否显示文本新增/修改弹窗
      open: false,
      // 是否显示文档上传弹窗
      uploadOpen: false,
      // 文档上传中
      uploading: false,
      // ES 向量刷新中
      refreshing: false,
      // 待上传的文件对象
      uploadFile: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        category: null,
        title: null,
        keywords: null,
        content: null,
        status: 0,
      },
      // 文本表单参数
      form: {},
      // 文本表单校验
      rules: {
        title: [
          { required: true, message: "知识点标题不能为空", trigger: "blur" }
        ],
        content: [
          { required: true, message: "详细内容不能为空", trigger: "blur" }
        ],
      },
      // 文档上传表单
      uploadForm: {
        title: '',
        category: '',
        keywords: '',
      },
      // 文档上传表单校验
      uploadRules: {
        title: [
          { required: true, message: "知识标题不能为空", trigger: "blur" }
        ],
      },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询签证知识库列表 */
    getList() {
      this.loading = true
      listKnowledge(this.queryParams).then(response => {
        this.knowledgeList = response.rows
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
        category: null,
        title: null,
        keywords: null,
        content: null,
        status: 0,
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
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增文本知识条目 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加签证知识库"
    },
    /** 打开文档上传弹窗 */
    handleUploadDoc() {
      this.uploadForm = { title: '', category: '', keywords: '' }
      this.uploadFile = null
      this.uploadOpen = true
    },
    /** 文件选中回调 */
    handleFileChange(file) {
      this.uploadFile = file.raw
    },
    /** 文件移除回调 */
    handleFileRemove() {
      this.uploadFile = null
    },
    /** 提交文档上传 */
    submitUpload() {
      this.$refs["uploadForm"].validate(valid => {
        if (!valid) return
        if (!this.uploadFile) {
          this.$modal.msgWarning("请选择要上传的文档文件")
          return
        }
        this.uploading = true
        const formData = new FormData()
        formData.append('file', this.uploadFile)
        formData.append('title', this.uploadForm.title)
        if (this.uploadForm.category) formData.append('category', this.uploadForm.category)
        if (this.uploadForm.keywords) formData.append('keywords', this.uploadForm.keywords)
        uploadKnowledgeDoc(formData).then(response => {
          this.$modal.msgSuccess("文档摄取成功，向量已写入 Elasticsearch")
          this.uploadOpen = false
          this.getList()
        }).catch(err => {
          this.$modal.msgError("文档摄取失败：" + (err.message || '请查看后端日志'))
        }).finally(() => {
          this.uploading = false
        })
      })
    },
    /** 取消文档上传 */
    cancelUpload() {
      this.uploadOpen = false
      this.uploadFile = null
    },
    /** 一键刷新 Elasticsearch 向量索引 */
    handleRefreshEmbeddings() {
      this.$modal.confirm('将对所有有效知识条目重新生成语义向量并写入 Elasticsearch，确认执行？').then(() => {
        this.refreshing = true
        refreshEmbeddings().then(response => {
          this.$modal.msgSuccess(response.msg || "向量刷新任务已提交，请稍后查看后端日志")
        }).finally(() => {
          this.refreshing = false
        })
      }).catch(() => {})
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getKnowledge(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改签证知识库"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateKnowledge(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addKnowledge(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除签证知识库编号为"' + ids + '"的数据项？').then(function() {
        return delKnowledge(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('visa/knowledge/export', {
        ...this.queryParams
      }, `knowledge_${new Date().getTime()}.xlsx`)
    },
    /** 状态修改逻辑 */
    handleStatusChange(row) {
      let text = row.status === 0 ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '""' + row.title + '"这一知识点吗？').then(function() {
        return updateKnowledge({ id: row.id, status: row.status });
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === 0 ? 1 : 0;
      });
    },
  }
}
</script>

