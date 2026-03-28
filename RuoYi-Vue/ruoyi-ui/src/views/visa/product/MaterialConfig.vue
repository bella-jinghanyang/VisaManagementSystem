/** 签证产品材料上传 */
<template>
  <div class="material-config">
    <div class="mb10">
      <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd">添加材料项</el-button>
    </div>

    <!-- 1. 材料名称 -->
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column label="材料名称" prop="name">
        <template slot-scope="scope">
          <el-input v-model="scope.row.name" placeholder="如:护照首页" />
        </template>
      </el-table-column>

      <!-- 2. 数据类型 -->
      <el-table-column label="提交类型" prop="type" width="140">
        <template slot-scope="scope">
          <el-select v-model="scope.row.type" placeholder="请选择">
            <el-option label="图片" value="image" />
            <el-option label="纯文本" value="text" />
            <el-option label="文件(PDF)" value="file" />
          </el-select>
        </template>
      </el-table-column>

      <!-- 3. 适用人群 -->
      <el-table-column label="适用人群" prop="targetGroup" width="150">
        <template slot-scope="scope">
          <el-select v-model="scope.row.targetGroup" placeholder="所有人">
            <el-option label="所有人" value="ALL" />
            <el-option label="在职人员" value="EMPLOYED" />
            <el-option label="自由职业" value="FREELANCE" />
            <el-option label="无业人员/家庭主妇" value="UNEMPLOYED" />
            <el-option label="学生" value="STUDENT" />
            <el-option label="退休人员" value="RETIRED" />
            <el-option label="学龄前儿童" value="CHILD" />
          </el-select>
        </template>
      </el-table-column>

      <!-- 3. 是否必填 -->
      <el-table-column label="是否必填" width="100" align="center">
        <template slot-scope="scope">
          <el-switch v-model="scope.row.required" />
        </template>
      </el-table-column>

      <!-- 4. 说明备注 -->
      <el-table-column label="说明备注" prop="desc" width="200">
        <template slot-scope="scope">
          <el-input v-model="scope.row.desc" placeholder="如:不能反光" />
        </template>
      </el-table-column>

      <!-- 5. 模板上传列 -->
      <el-table-column label="示例/模板(可选)" min-width="150">
        <template slot-scope="scope">
          <!-- 如果已经上传了，显示文件名和删除按钮 -->
          <div v-if="scope.row.templateUrl" class="file-box">
            <i class="el-icon-document"></i>
            <span class="filename" :title="scope.row.templateName">{{ scope.row.templateName }}</span>
            <el-button type="text" class="red" icon="el-icon-delete" @click="removeTemplate(scope.row)"></el-button>
          </div>

          <!-- 如果没上传，显示上传按钮 -->
          <el-upload v-else class="upload-demo" :action="uploadUrl" :headers="headers" :show-file-list="false"
            :on-success="(res) => handleUploadSuccess(res, scope.row)" :on-error="handleUploadError">
            <el-button size="mini" type="info" plain icon="el-icon-upload2">上传模板</el-button>
          </el-upload>
        </template>
      </el-table-column>

      <!-- 6. 操作 -->
      <el-table-column label="操作" width="80" align="center">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-delete" class="red" @click="handleDelete(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getToken } from "@/utils/auth";

export default {
  name: "MaterialConfig",
  // 接收父组件传来的 JSON 字符串
  props: {
    value: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      tableData: [],
      // 若依通用的上传接口地址
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      // 设置请求头，携带 Token
      headers: {
        Authorization: "Bearer " + getToken()
      }
    };
  },
  watch: {
    // 监听 tableData 的变化，一旦变化，就转成 JSON 字符串发给父组件
    tableData: {
      deep: true,
      handler(val) {
        this.$emit("input", JSON.stringify(val));
      }
    },
    // 监听父组件传进来的 value (用于回显数据)
    value: {
      immediate: true,
      handler(val) {
        try {
          // 如果有值，尝试解析 JSON；如果是空，给个空数组
          this.tableData = val ? JSON.parse(val) : [];
        } catch (e) {
          this.tableData = [];
        }
      }
    }
  },
  methods: {
    handleAdd() {
      this.tableData.push({
        name: "",
        type: "image",
        required: true,
        desc: "",
        templateName: "",
        templateUrl: "",
        targetGroup: "ALL"
      });
    },
    handleDelete(index) {
      this.tableData.splice(index, 1);
    },
    // 上传成功的回调
    handleUploadSuccess(res, row) {
      if (res.code === 200) {
        // 若依返回的 res 包含 url 和 fileName
        this.$set(row, 'templateUrl', res.url); // 这里的 res.url 已经包含了 /profile/...
        this.$set(row, 'templateName', res.originalFilename); // 获取原始文件名
        this.$message.success("模板上传成功");
      } else {
        this.$message.error(res.msg);
      }
    },
    handleUploadError() {
      this.$message.error("上传失败，请检查网络");
    },
    // 删除模板
    removeTemplate(row) {
      row.templateUrl = "";
      row.templateName = "";
    }
  }
};
</script>

<style scoped>
.mb10 {
  margin-bottom: 10px;
}

.tip-text {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}

.red {
  color: #ff4949;
}

.file-box {
  display: flex;
  align-items: center;
  font-size: 12px;
  background: #f4f4f5;
  padding: 2px 5px;
  border-radius: 4px;
}

.filename {
  margin: 0 5px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #409EFF;
}
</style>