<template>
  <div class="profile-page container-width">
    <div class="page-header">
      <h2>个人设置</h2>
      <p>管理您的档案信息</p>
    </div>

    <div class="profile-layout">
      <!-- 左侧：头像卡片 -->
      <div class="left-col">
        <apple-card class="avatar-card">
          <div class="avatar-wrapper">
            <!-- 显示头像 -->
            <el-avatar :size="100" :src="$getImgUrl(form.avatar) || defaultAvatar"></el-avatar>

            <!-- 头像上传组件 -->
            <el-upload class="avatar-uploader" :action="uploadUrl" :headers="headers" :show-file-list="false"
              :on-success="handleAvatarSuccess" name="file">
              <div class="edit-mask"><i class="el-icon-camera"></i></div>
            </el-upload>
          </div>
          <h3>{{ form.nickname || '未设置昵称' }}</h3>
          <p>{{ form.phone }}</p>
        </apple-card>
      </div>

      <!-- 右侧：表单卡片 -->
      <div class="right-col">
        <apple-card>
          <h3 class="card-title">基本信息</h3>

          <el-form :model="form" label-position="top" class="custom-form">
            <el-row :gutter="30">
              <el-col :span="12">
                <el-form-item label="昵称">
                  <el-input v-model="form.nickname" placeholder="请输入昵称"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="真实姓名">
                  <el-input v-model="form.realName" placeholder="下单自动回填"></el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="30">
              <el-col :span="12">
                <el-form-item label="手机号">
                  <el-input v-model="form.phone" disabled placeholder="手机号不支持修改"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="身份证号">
                  <el-input v-model="form.idCard" placeholder="下单自动回填"></el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="30">
               <el-col :span="12">
                <el-form-item label="性别">
              <el-radio-group v-model="form.sex">
                <el-radio label="0">男</el-radio>
                <el-radio label="1">女</el-radio>
                <el-radio label="2">未知</el-radio>
              </el-radio-group>
            </el-form-item>
               </el-col>

            <el-col :span="12">
              <el-form-item label="电子邮箱">
              <el-input v-model="form.email" placeholder="接收通知邮件"></el-input>
            </el-form-item>
            </el-col>

            </el-row>

            <div class="btn-row">
              <el-button type="primary" :loading="loading" @click="handleSave">保存修改</el-button>
            </div>
          </el-form>
        </apple-card>
      </div>
    </div>
  </div>
</template>

<script>
import AppleCard from '@/components/AppleCard'
import { getUserProfile, updateUserProfile } from '@/api/user'

export default {
  name: 'Profile',
  components: { AppleCard },
  data () {
    return {
      loading: false,
      defaultAvatar: require('@/assets/images/user_default.jpg'), // 确保你assets里有这张默认图

      // 上传相关配置
      uploadUrl: '/api/common/upload', // 若依通用上传接口
      headers: {
        Authorization: 'Bearer ' + localStorage.getItem('Client-Token')
      },

      form: {
        id: '',
        nickname: '',
        realName: '',
        phone: '',
        idCard: '',
        email: '',
        avatar: '',
        sex: ''
      }
    }
  },
  created () {
    this.initData()
  },
  methods: {
    // 初始化数据
    initData () {
      // 1. 先从 LocalStorage 拿 ID
      const userStr = localStorage.getItem('Client-User')
      if (userStr) {
        const localUser = JSON.parse(userStr)
        // 2. 用 ID 去后端查最新的数据 (防止多端登录数据不一致)
        getUserProfile(localUser.id).then(res => {
          this.form = res.data

          if (!this.form.sex) {
            this.$set(this.form, 'sex', '2')
          }

          // 顺便更新一下本地缓存，保持最新
          this.refreshLocalStorage(res.data)
        })
      }
    },

    // 头像上传成功回调
    handleAvatarSuccess (res) {
      if (res.code === 200) {
        // ★★★ 1. 打印看看后端到底返回了什么 ★★★
        console.log('上传接口返回结果：', res)

        // ★★★ 2. 这里的字段名很关键！ ★★★
        // 若依标准版返回的是 fileName (相对路径)
        // 有些二开版本可能返回的是 url
        // 我们做一个兼容处理：
        const avatarPath = res.fileName || res.url

        // 赋值 (使用 $set 保证 Vue 能监听到变化，从而刷新图片)
        this.$set(this.form, 'avatar', avatarPath)

        this.$message.success('头像上传成功，请点击保存')

        // ★★★ 3. 再次打印确认赋值成功没 ★★★
        console.log('当前表单里的头像值：', this.form.avatar)
      } else {
        this.$message.error(res.msg)
      }
    },

    // 保存修改
    handleSave () {
      this.loading = true
      updateUserProfile(this.form).then(res => {
        this.$message.success('保存成功')

        // 1. 更新本地缓存
        this.refreshLocalStorage(this.form)

        // 2. 通知 Navbar 更新右上角显示
        this.$bus.$emit('login-success')

        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },

    // 辅助方法：更新本地缓存
    refreshLocalStorage (userData) {
      localStorage.setItem('Client-User', JSON.stringify(userData))
    }
  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

.profile-page {
  padding-top: 40px;
  padding-bottom: 60px;
}

.page-header {
  margin-bottom: 30px;

  h2 {
    font-weight: 700;
    color: $text-main;
  }

  p {
    color: $text-secondary;
    font-size: 14px;
  }
}

.profile-layout {
  display: flex;
  gap: 30px;
}

.left-col {
  width: 300px;
}

.right-col {
  flex: 1;
}

.avatar-card {
  text-align: center;
  padding: 40px 20px !important;

  .avatar-wrapper {
    position: relative;
    width: 100px;
    height: 100px;
    margin: 0 auto 20px;
    border-radius: 50%;
    overflow: hidden;
    cursor: pointer;

    /* 鼠标移上去显示相机遮罩 */
    &:hover .edit-mask {
      opacity: 1;
    }

    .edit-mask {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      font-size: 24px;
    }
  }

  h3 {
    margin-bottom: 5px;
    color: $text-main;
  }

  p {
    color: $text-secondary;
    font-size: 14px;
  }
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.custom-form {
  ::v-deep .el-input__inner {
    height: 45px;
  }

  .btn-row {
    text-align: right;
    margin-top: 20px;
  }
}
</style>
