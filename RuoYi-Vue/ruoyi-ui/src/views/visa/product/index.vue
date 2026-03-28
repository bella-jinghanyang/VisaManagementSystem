<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="120px">
      <el-form-item label="产品标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入产品标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="销售价格" prop="price">
        <el-input v-model="queryParams.price" placeholder="请输入销售价格" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="是否需要面签" prop="isInterviewRequired">
        <el-input v-model="queryParams.isInterviewRequired" placeholder="请输入是否需要面签" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="预计办理时长" prop="processingTime">
        <el-input v-model="queryParams.processingTime" placeholder="请输入预计办理时长" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="有效期" prop="validityPeriod">
        <el-input v-model="queryParams.validityPeriod" placeholder="请输入有效期" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="最长停留天数" prop="maxStayDays">
        <el-input v-model="queryParams.maxStayDays" placeholder="请输入最长停留天数" clearable
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
          v-hasPermi="['visa:product:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['visa:product:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete"
          v-hasPermi="['visa:product:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['visa:product:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="产品标题" align="center" prop="title" width="220" />
      <el-table-column label="国家" align="center" prop="countryName" width="100" />
      <el-table-column label="类型" align="center" prop="typeName" width="110" />
      <el-table-column label="销售价格" align="center" prop="price" width="110" />
      <el-table-column label="产品封面图片" align="center" prop="image" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.image" :width="50" :height="50" />
        </template>
      </el-table-column>
      <el-table-column label="所需材料" align="center" width="120">
        <template slot-scope="scope">
          <el-popover placement="right" width="450" trigger="hover">
            <!-- 悬浮层里的内容 -->
            <el-table :data="parseMaterials(scope.row.requirementsConfig)" border size="mini">
              <el-table-column width="100" property="name" label="名称"></el-table-column>
              <el-table-column width="80" property="type" label="类型">
                <template slot-scope="s">
                  <span v-if="s.row.type === 'image'">图片</span>
                  <span v-else-if="s.row.type === 'file'">文件</span>
                  <span v-else>文本</span>
                </template>
              </el-table-column>
              <el-table-column width="100" label="适用人群">
                <template slot-scope="s">
                  <el-tag size="mini" :type="s.row.targetGroup === 'ALL' ? 'info' : 'success'">
                    {{ getIdentityLabel(s.row.targetGroup) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column property="required" label="必填">
                <template slot-scope="s">{{ s.row.required ? '是' : '否' }}</template>
              </el-table-column>
            </el-table>

            <!-- 表格里显示的按钮 -->
            <el-button slot="reference" size="mini" type="text" icon="el-icon-view">查看配置</el-button>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column label="是否面签" align="center" prop="isInterviewRequired">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isInterviewRequired === 1" type="warning">需要面签</el-tag>
          <el-tag v-else type="success">免面试</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预计办理时长" align="center" prop="processingTime" width="110" />
      <el-table-column label="有效期" align="center" prop="validityPeriod" width="110" />
      <el-table-column label="最长停留天数" align="center" prop="maxStayDays" width="110" />
      <el-table-column label="领区" align="center" prop="districtName" width="110" />
      <el-table-column label="上架状态" align="center" prop="status" width="160">
        <template slot-scope=" scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" active-text="上架"
            inactive-text="下架" @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" fixed="right" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['visa:product:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['visa:product:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改签证产品对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1000px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="产品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入产品标题" />
        </el-form-item>
        <el-form-item label="国家" prop="countryId">
          <el-select v-model="form.countryId" placeholder="请选择国家">
            <el-option v-for="item in countryOptions" :key="item.id"
              :label="item.name + (item.status === '0' ? ' (已下架)' : '')" :value="item.id"
              :disabled="item.status === '0'" />
          </el-select>
        </el-form-item>
        <el-form-item label="领区" prop="districtId">
          <el-select v-model="form.districtId" placeholder="请选择领区">
            <el-option v-for="item in districtOptions" :key="item.id"
              :label="item.name + (item.status === '0' ? ' (已下架)' : '')" :value="item.id"
              :disabled="item.status === '0'" />
          </el-select>
        </el-form-item>
        <el-form-item label="签证类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择签证类型">
            <el-option v-for="item in typeOptions" :key="item.id"
              :label="item.name + (item.status === '0' ? ' (已下架)' : '')" :value="item.id"
              :disabled="item.status === '0'" />
          </el-select>
        </el-form-item>
        <el-form-item label="销售价格" prop="price">
          <el-input v-model="form.price" placeholder="请输入销售价格" />
        </el-form-item>
        <el-form-item label="产品封面图片" prop="image">
          <image-upload v-model="form.image" />
        </el-form-item>
        <el-form-item label="所需材料配置" prop="requirementsConfig">
          <material-config v-model="form.requirementsConfig" />
        </el-form-item>
        <el-form-item label="是否需要面签" prop="isInterviewRequired">
          <el-select v-model="form.isInterviewRequired" placeholder="请选择是否需要面签" clearable>
            <el-option label="是" :value="1"></el-option>
            <el-option label="否" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预计办理时长" prop="processingTime">
          <el-input v-model="form.processingTime" placeholder="请输入预计办理时长" />
        </el-form-item>
        <el-form-item label="有效期" prop="validityPeriod">
          <el-input v-model="form.validityPeriod" placeholder="请输入有效期" />
        </el-form-item>
        <el-form-item label="最长停留天数" prop="maxStayDays">
          <el-input v-model="form.maxStayDays" placeholder="请输入最长停留天数" />
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
import { listProduct, getProduct, delProduct, addProduct, updateProduct, changeProductStatus } from "@/api/visa/product"
import { listCountry } from "@/api/visa/country";
import { listType } from "@/api/visa/type";
import { listDistrict } from "@/api/visa/district";
import MaterialConfig from "./MaterialConfig";

export default {
  name: "Product",
  components: {
    MaterialConfig
  },
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
      // 签证产品表格数据
      productList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: null,
        countryId: null,
        typeId: null,
        price: null,
        image: null,
        requirementsConfig: null,
        isInterviewRequired: null,
        processingTime: null,
        validityPeriod: null,
        maxStayDays: null,
        districtId: null,
        status: null,
      },
      countryOptions: [],
      typeOptions: [],
      districtOptions: [],
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        title: [
          { required: true, message: "产品标题不能为空", trigger: "blur" }
        ],
        countryId: [
          { required: true, message: "关联国家ID不能为空", trigger: "change" }
        ],
        typeId: [
          { required: true, message: "关联类型ID不能为空", trigger: "change" }
        ],
        price: [
          { required: true, message: "销售价格不能为空", trigger: "blur" }
        ],
        isInterviewRequired: [
          { required: true, message: "是否需要面签不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "上架状态不能为空", trigger: "change" }
        ],
      }
    }
  },
  created() {
    this.getList();
    this.getOptions();
  },
  methods: {
    /** 查询签证产品列表 */
    getList() {
      this.loading = true
      listProduct(this.queryParams).then(response => {
        this.productList = response.rows
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
        title: null,
        countryId: null,
        typeId: null,
        price: null,
        image: null,
        requirementsConfig: null,
        isInterviewRequired: null,
        processingTime: null,
        validityPeriod: null,
        maxStayDays: null,
        districtId: null,
        status: null,
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
      this.title = "添加签证产品"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getProduct(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改签证产品"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateProduct(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addProduct(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除签证产品编号为"' + ids + '"的数据项？').then(function () {
        return delProduct(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => { })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('visa/product/export', {
        ...this.queryParams
      }, `product_${new Date().getTime()}.xlsx`)
    },
    // 查询所有选项
    getOptions() {
      // 定义查询参数
      const queryParams = {
        pageNum: 1,      // 默认若依接口是分页的，不传可能只给10条
        pageSize: 1000   // 设大一点，保证把所有上架的国家都查出来
      };

      listCountry(queryParams).then(res => this.countryOptions = res.rows);
      listType(queryParams).then(res => this.typeOptions = res.rows);
      listDistrict(queryParams).then(res => this.districtOptions = res.rows);
    },
    getIdentityLabel(val) {
      const map = {
        'ALL': '所有人',
        'EMPLOYED': '在职',
        'FREELANCE': '自由职业',
        'UNEMPLOYED': '无业/主妇',
        'STUDENT': '学生',
        'RETIRED': '退休',
        'CHILD': '学龄前'
      };
      return map[val] || '所有人';
    },
    // 辅助函数：安全解析 JSON
    parseMaterials(jsonStr) {
      if (!jsonStr) return [];
      try {
        return JSON.parse(jsonStr);
      } catch (e) {
        return [];
      }
    },
    /** 上架/下架开关 */
    // handleStatusChange(row) {
    //   const oldStatus = row.status === 1 ? 0 : 1; // 因为 switch 已经切完了，所以 old 是反过来的
    //   const text = row.status === 1 ? "上架" : "下架";

    //   this.$modal.confirm(`确认要${text}【${row.title}】吗？`)
    //     .then(() => {
    //       return changeProductStatus(row.id, row.status);
    //     })
    //     .then(() => {
    //       this.$modal.msgSuccess(`${text}成功`);
    //     })
    //     .catch(() => {
    //       // 回滚
    //       row.status = oldStatus;
    //     });

    // }
    handleStatusChange(row) {
      let text = row.status === 1 ? "上架" : "下架";
      // 记录旧状态，用于失败回滚
      let oldStatus = row.status === 1 ? 0 : 1;

      this.$modal.confirm('确认要"' + text + '"产品【' + row.title + '】吗？')
        .then(() => {
          // 这里的 changeProductStatus 对应 api 文件里的方法
          return changeProductStatus(row.id, row.status);
        })
        .then(() => {
          this.$modal.msgSuccess(text + "成功");
        })
        .catch(() => {
          // ★★★ 关键：如果报错（比如后端拦截了），必须把状态改回去，否则前端看着是开的，实际库里是关的
          row.status = oldStatus;
        });
    }
  }
}
</script>
