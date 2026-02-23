<template>
  <div class="glass-nav">
    <div class="nav-content">
      <!-- LOGO -->
      <div class="logo">
        <i class="el-icon-s-promotion" style="color: #6AAFE6; margin-right: 8px;"></i>
        <span>Global Visa</span>
      </div>

      <!-- 菜单 -->
      <div class="menu">
        <router-link to="/" class="nav-item" exact>首页</router-link>
        <router-link to="/products" class="nav-item">签证大厅</router-link>
        <router-link v-if="isLogin" to="/user/orders" class="nav-item">
          <el-badge :value="orderCount" :hidden="orderCount === 0" class="badge-item">
            <span>我的订单</span>
          </el-badge>
        </router-link>
        <router-link to="/ai-chat" class="nav-item">智能助手</router-link>
      </div>

     <!-- 右侧 -->
      <div class="auth">
        <!-- ★★★ 情况A：已登录 (显示头像和下拉菜单) ★★★ -->
        <div v-if="isLogin" class="user-area">
          <!-- 购物车图标 -->
        <div class="cart-btn" @click="$router.push('/cart')">
          <el-badge :value="cartCount" :hidden="cartCount === 0" class="badge-item">
            <i class="el-icon-shopping-cart-2"></i>
          </el-badge>
        </div>
          <span class="username">Hi, {{ userInfo.nickname || userInfo.phone }}</span>

          <el-dropdown @command="handleCommand" trigger="click">
            <span class="el-dropdown-link">
              <!-- 这里用了 Element 的 Avatar 组件 -->
              <el-avatar :size="36" :src="$getImgUrl(userInfo.avatar)" class="avatar"></el-avatar>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile" icon="el-icon-user">个人主页</el-dropdown-item>
              <!-- <el-dropdown-item command="order" icon="el-icon-s-order">我的订单</el-dropdown-item> -->
              <el-dropdown-item command="logout" icon="el-icon-switch-button" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>

        <!-- ★★★ 情况B：未登录 (显示按钮) ★★★ -->
        <div v-else>
          <el-button type="primary" size="small" round @click="$router.push('/login')">
            登录 / 注册
          </el-button>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { getCartCount } from '@/api/cart'
import { getPendingOrderCount } from '@/api/order'
export default {
  data () {
    return {
      cartCount: 0,
      isLogin: false,
      userInfo: {},
      orderCount: 0 // 待办订单数
    }
  },
  created () {
    // 页面一加载，先查一次
    this.checkLogin()
    this.refreshCounts()

    // ★★★ 3. 监听登录成功事件 ★★★
    // 只要别的组件发出了 login-success，这里就会执行 checkLogin
    this.$bus.$on('login-success', () => {
      this.checkLogin()
      this.refreshCounts()
    })

    this.refreshCartCount()
    // 监听更新事件
    this.$bus.$on('cart-updated', () => {
      this.refreshCartCount()
    })

    // 监听事件：当订单状态改变或新下单时，刷新数字
    this.$bus.$on('order-updated', () => {
      this.refreshCounts()
    })
  },
  // 养成好习惯，组件销毁时解绑事件
  beforeDestroy () {
    this.$bus.$off('login-success')
  },
  methods: {
    checkLogin () {
      const token = localStorage.getItem('Client-Token')
      const userStr = localStorage.getItem('Client-User')

      // 只有 Token 和 用户信息 都有，才算登录
      if (token && userStr) {
        this.isLogin = true
        try {
          this.userInfo = JSON.parse(userStr)
        } catch (e) {
          console.error('用户信息解析失败', e)
          this.isLogin = false
        }
      } else {
        this.isLogin = false
        this.userInfo = {}
      }
    },
    handleCommand (cmd) {
      if (cmd === 'logout') {
        // --- 退出登录逻辑 ---
        localStorage.removeItem('Client-Token')
        localStorage.removeItem('Client-User')

        // 更新视图状态
        this.isLogin = false
        this.userInfo = {}

        if (this.$route.path !== '/') {
          this.$router.push('/').catch(err => { console.error(err) })
        } else {
          // 如果已经在首页了，就刷新一下页面，清除页面上的个人数据
          // location.reload(); // 或者啥也不干
          console.log('已经在首页，无需重复跳转')
        }

        this.$message.success('已退出登录')
        // 通知 App.vue 活跃时间重置
        this.$bus.$emit('order-updated')
      } else if (cmd === 'order') {
        this.$router.push('/user/orders')
      } else if (cmd === 'profile') {
        this.$router.push('/user/profile')
      }
    },

    refreshCartCount () {
      const userStr = localStorage.getItem('Client-User')
      if (userStr) {
        const user = JSON.parse(userStr)
        getCartCount(user.id).then(res => {
          this.cartCount = res.data // 后端返回的是数字
        })
      }
    },

    refreshCounts () {
      const userStr = localStorage.getItem('Client-User')
      if (userStr) {
        const user = JSON.parse(userStr)
        // 同时刷新购物车和订单数
        this.refreshCartCount(user.id)

        // ★ 获取订单数
        getPendingOrderCount(user.id).then(res => {
          this.orderCount = res.data
        })
      }
    }
  }
}
</script>

<style scoped lang="scss">
// 引入变量
@import "@/assets/styles/variables.scss";

.glass-nav {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 70px;
  z-index: 999;

  /* 核心：毛玻璃效果 */
  background: rgba(255, 255, 255, 0.75); // 75%透明度的白
  backdrop-filter: saturate(180%) blur(20px); // 高斯模糊
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.02); // 极淡的阴影
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
}

.nav-content {
  width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 22px;
  font-weight: 800;
  color: $text-main;
  display: flex;
  align-items: center;
}

.nav-item {
  margin: 0 20px;
  text-decoration: none;
  color: $text-secondary;
  font-weight: 500;
  font-size: 16px;
  position: relative;
  transition: color 0.3s;

  &:hover {
    color: $primary-color;
  }

  /* 选中状态下，底部出现一个小圆点 */
  &.router-link-active {
    color: $text-main;
    font-weight: 700;

    &::after {
      content: '';
      position: absolute;
      bottom: -10px;
      left: 50%;
      transform: translateX(-50%);
      width: 6px;
      height: 6px;
      background: $primary-color;
      border-radius: 50%;
      box-shadow: 0 0 10px $primary-color;
    }
  }
}

.user-area {
  display: flex;
  align-items: center;
  cursor: pointer;

  .username {
    margin-right: 12px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }

  .avatar {
    border: 2px solid #fff;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s;

    &:hover {
      transform: scale(1.1);
    }
  }
}

.cart-btn {
  margin-right: 25px;
  cursor: pointer;

  transition: all 0.3s ease;

  &:hover {
    transform: scale(1.1); // 鼠标移上去轻轻放大
    opacity: 0.8;
  }

  &:active {
    transform: scale(0.9); // 点击时微微缩小，有物理按压感
  }
}

/* 调整角标颜色为奶蓝主题色 */
::v-deep .el-badge__content {

  border: none;
}
</style>
