<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
export default {
  name: 'App',
  data () {
    return {
      lastTime: new Date().getTime(), // 最后一次活跃时间
      currentTime: new Date().getTime(),
      timeout: 30 * 60 * 1000, // 设置超时时间：30分钟
      checkTimer: null
    }
  },
  mounted () {
    // 监听鼠标移动、点击、键盘按下等事件
    window.addEventListener('mouseover', this.updateLastTime)
    window.addEventListener('click', this.updateLastTime)
    window.addEventListener('keydown', this.updateLastTime)
    window.addEventListener('scroll', this.updateLastTime)

    // 启动一个定时炸弹，每分钟检查一次是否过期
    this.checkTimer = setInterval(this.checkTimeout, 60000)
  },
  beforeDestroy () {
    // 组件销毁前移除监听，防止内存溢出
    window.removeEventListener('mouseover', this.updateLastTime)
    window.removeEventListener('click', this.updateLastTime)
    window.removeEventListener('keydown', this.updateLastTime)
    window.removeEventListener('scroll', this.updateLastTime)
    clearInterval(this.checkTimer)
  },
  methods: {
    // 只要用户动了，就刷新最后活跃时间
    updateLastTime () {
      this.lastTime = new Date().getTime()
    },
    // 检查逻辑
    checkTimeout () {
      // 如果用户根本没登录，就不检查
      if (!localStorage.getItem('Client-Token')) return

      this.currentTime = new Date().getTime()
      if (this.currentTime - this.lastTime > this.timeout) {
        // --- 超过30分钟没动了，执行自动登出 ---
        this.autoLogout()
      }
    },
    autoLogout () {
      // 1. 清除本地缓存
      localStorage.removeItem('Client-Token')
      localStorage.removeItem('Client-User')

      // 2. 提示用户
      this.$alert('由于您长时间未操作，为了账户安全，请重新登录', '会话已过期', {
        confirmButtonText: '确定',
        callback: action => {
          // 3. 强行跳回登录页
          this.$router.push('/login')
          // 4. 通知 Navbar 刷新状态
          this.$bus.$emit('login-success')
        }
      })
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

</style>
