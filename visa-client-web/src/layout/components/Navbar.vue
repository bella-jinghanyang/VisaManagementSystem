<template>
  <div :class="['glass-nav', { 'is-transparent': isTransparent && isHomePage }]">
    <div class="nav-content">
      <!-- LOGO 部分 -->
      <div class="logo">
        <!-- 颜色跟随主题色 -->
        <i class="el-icon-s-promotion icon-brand"></i>
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
        <router-link v-if="isLogin" to="/specialist-chat" class="nav-item">
          <span>专属顾问</span>
        </router-link>
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
/* eslint-disable */
import { getCartCount } from '@/api/cart';
import { getPendingOrderCount } from '@/api/order';
export default {
  data () {
    return {
      cartCount: 0,
      isLogin: false,
      userInfo: {},
      orderCount: 0,// 待办订单数
      isTransparent: true, // 是否透明
      isHomePage: false,   // 是否是首页
    }
  },
  watch: {
    // 监听路由变化，只有首页才需要透明融合效果
    '$route': {
      immediate: true,
      handler(to) {
        this.isHomePage = to.path === '/';
        this.handleScroll();
      }
    }
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll);
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
     handleScroll() {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      // 滚动超过 50px 就取消透明
      this.isTransparent = scrollTop < 50;
    },
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
/* 引入变量 */
@import "@/assets/styles/variables.scss";

.glass-nav {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 72px;
  z-index: 1000;
  display: flex;
  align-items: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  
  /* --- 状态 A: 滚动后（白色磨砂质感） --- */
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);

  .logo, .nav-item, .username, .cart-btn i {
    color: #0f172a; // 使用深石板色
  }

  /* --- 状态 B: 首页顶部透明态 (is-transparent) --- */
  &.is-transparent {
    background: transparent !important;
    backdrop-filter: blur(0) !important;
    box-shadow: none !important;
    border-bottom: 1px solid transparent !important;
    height: 80px; // 顶部时稍微宽一点，更大气
    
    /* 
       关键修改：
       因为你的背景现在是浅色（白色/浅灰），所以文字不能是白色！
       我们要用深色文字，或者主色调文字。
    */
    .logo { color: #0f172a; }
    .nav-item { color: #475569; } // 稍淡一点的灰色
    .username { color: #475569; }
    .cart-btn i { color: #475569; }

    /* 如果你觉得还是不清晰，可以给文字加极淡的文字阴影，或者给Nav加一层极其微弱的顶部渐变（可选） */
  }
}

.nav-content {
  width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

/* Logo 样式 */
.logo {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  text-transform: uppercase; // 品牌名大写更高级
  
  .icon-brand {
    color: $primary-color !important; // 使用你新定的科技蓝
    margin-right: 10px;
    font-size: 24px;
  }
}

/* 导航项 */
.menu {
  display: flex;
  align-items: center;
  
  .nav-item {
    margin: 0 18px;
    text-decoration: none;
    font-weight: 600;
    font-size: 15px;
    position: relative;
    transition: all 0.3s ease;

    &:hover {
      color: $primary-color !important;
    }

    &.router-link-active {
      color: $primary-color !important;
      &::after {
        content: '';
        position: absolute;
        bottom: -8px; left: 50%;
        transform: translateX(-50%);
        width: 4px; height: 4px;
        background: $primary-color;
        border-radius: 50%;
      }
    }
  }
}

/* 用户区域 */
.user-area {
  display: flex;
  align-items: center;
  
  .cart-btn {
    margin-right: 20px;
    i { font-size: 20px; transition: color 0.3s; }
    &:hover i { color: $primary-color; }
  }

  .username {
    font-size: 14px;
    margin-right: 12px;
    font-weight: 600;
  }

  .avatar {
    border: 2px solid #fff;
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
    cursor: pointer;
    transition: transform 0.3s ease;
    &:hover { transform: translateY(-2px); }
  }
}

/* 登录按钮在透明态下的适配 */
.glass-nav.is-transparent .el-button--primary {
  background: $primary-color !important;
  border-color: $primary-color !important;
  box-shadow: 0 4px 12px rgba(0, 98, 255, 0.2);
}

/* 角标样式微调 */
::v-deep .el-badge__content {
  background-color: $primary-color; // 角标用主色
  border: none;
  font-family: Arial;
}
</style>
