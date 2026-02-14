import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import '@/assets/styles/index.scss'

Vue.config.productionTip = false
Vue.use(ElementUI) // 全局注册

// 全局图片处理函数
Vue.prototype.$getImgUrl = function (url) {
  // 1. 如果 url 为空，返回一个默认占位图 (建议你在 assets 放一张 no-image.png)
  if (!url) return require('@/assets/images/no-image.jpg') // 如果没有这张图，这行先注释掉

  // 2. 如果是网络图片 (http开头)，直接返回
  if (url.indexOf('http') === 0 || url.indexOf('https') === 0) {
    return url
  }

  // 3. 如果是本地图片，拼上后端地址
  // 这里写死 localhost:8080，或者读取环境变量 process.env.VUE_APP_BASE_API
  return 'http://localhost:8080' + url
}

Vue.prototype.$bus = new Vue()

new Vue({
  router,
  store,
  render: (h) => h(App)
}).$mount('#app')
