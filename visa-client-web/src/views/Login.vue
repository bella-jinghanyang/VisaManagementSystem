<template>
  <div class="login-bg">
    <div class="login-card">
      <!-- 标题 -->
      <div class="header">
        <h1 class="title">{{ isRegister ? '创建账号' : '欢迎回来' }}</h1>
        <p class="sub-title">Global Visa 全球签证服务平台</p>
      </div>

      <!-- 表单区域 -->
      <div class="form-area">
        <!-- 手机号 -->
        <el-input
          v-model="form.phone"
          placeholder="请输入手机号码"
          prefix-icon="el-icon-mobile-phone"
          class="custom-input mb-20"
        ></el-input>

        <!-- 密码 -->
        <el-input
          v-model="form.password"
          placeholder="请输入密码"
          prefix-icon="el-icon-lock"
          show-password
          type="password"
          class="custom-input mb-20"
        ></el-input>

        <!-- 注册时的确认密码 -->
        <el-input
          v-if="isRegister"
          v-model="confirmPassword"
          placeholder="请确认密码"
          prefix-icon="el-icon-check"
          show-password
          type="password"
          class="custom-input mb-20"
        ></el-input>

        <!-- 按钮 -->
        <el-button
          type="primary"
          class="action-btn"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ isRegister ? '立即注册' : '登 录' }}
        </el-button>

        <!-- 切换模式 -->
        <div class="toggle-box">
          <span v-if="!isRegister">还没有账号？ <a @click="isRegister = true">去注册</a></span>
          <span v-else>已有账号？ <a @click="isRegister = false">去登录</a></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { login, register } from '@/api/auth'

export default {
  name: 'Login',
  data () {
    return {
      isRegister: false, // 默认是登录模式
      loading: false,
      form: {
        phone: '',
        password: ''
      },
      confirmPassword: ''
    }
  },
  methods: {
    handleSubmit () {
      // 1. 简单校验
      if (!this.form.phone || !this.form.password) {
        this.$message.warning('请填写完整信息')
        return
      }
      if (this.isRegister && this.form.password !== this.confirmPassword) {
        this.$message.error('两次输入的密码不一致')
        return
      }

      this.loading = true

      // 2. 判断是登录还是注册
      if (this.isRegister) {
        // --- 注册逻辑 ---
        register(this.form).then(res => {
          this.$message.success('注册成功，请登录')
          this.isRegister = false // 切换回登录页
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      } else {
        // --- 登录逻辑 ---
        login(this.form).then(res => {
          this.$message.success('登录成功')

          // ★★★ 核心：保存 Token 和 用户信息 ★★★
          localStorage.setItem('Client-Token', res.token)
          // 把对象转成字符串存进去
          localStorage.setItem('Client-User', JSON.stringify(res.user))
          this.$bus.$emit('login-success')

          // 跳转回首页
          this.$router.push('/')
        }).catch(() => {
          this.loading = false
        })
      }
    }
  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

.login-bg {
  height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  width: 400px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.header {
  margin-bottom: 40px;
  .title { font-size: 28px; font-weight: 700; color: $text-main; margin-bottom: 10px; }
  .sub-title { color: $text-secondary; font-size: 14px; }
}

.mb-20 { margin-bottom: 20px; }

/* 覆盖 Element 输入框样式，让它更圆润 */
::v-deep .custom-input .el-input__inner {
  border-radius: 50px;
  height: 50px;
  padding-left: 40px;
  background-color: #F5F7FA;
  border: 1px solid transparent;
  &:focus { background-color: #fff; border-color: $primary-color; }
}

.action-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  border-radius: 50px;
  margin-top: 10px;
  box-shadow: $shadow-hover;
}

.toggle-box {
  margin-top: 20px;
  font-size: 14px;
  color: $text-secondary;
  a {
    color: $primary-color;
    cursor: pointer;
    font-weight: 600;
    margin-left: 5px;
    &:hover { text-decoration: underline; }
  }
}
</style>
