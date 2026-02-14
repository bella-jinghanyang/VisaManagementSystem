import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import router from '@/router'

// 1. 创建 axios 实例
const service = axios.create({
  // 注意：这里默认读取 vue.config.js 里的 proxy 代理
  // 如果你配置了 proxy: { '/api': ... }，这里就写 '/api'
  baseURL: '/api',
  // 超时时间
  timeout: 10000
})

// 2. 请求拦截器
service.interceptors.request.use(
  config => {
    // 从 LocalStorage 获取 C端 Token
    // 注意：别跟若依后台的 'Admin-Token' 搞混，这里我们用 'Client-Token'
    const token = localStorage.getItem('Client-Token')

    if (token) {
      // // 若依后端默认识别 Authorization: Bearer xxx
      // config.headers.Authorization = 'Bearer ' + token
      // 🔴 把原来的 Authorization 改成 Client-Token (或者别的名字)
    // 这样若依的 JwtFilter 就会因为找不到 Authorization 而跳过解析，不会报错
      config.headers['Client-Token'] = token
    }
    return config
  },
  error => {
    console.log(error)
    return Promise.reject(error)
  }
)

// 3. 响应拦截器
service.interceptors.response.use(
  response => {
    // 若依后端返回的数据结构通常是: { code: 200, msg: "...", data: ... }
    const res = response.data

    // 如果是二进制数据(文件下载)，直接返回
    if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
      return res
    }

    // 校验状态码
    if (res.code === 200) {
      return res
    } else {
      // --- 处理各种错误情况 ---

      // 401: Token 过期或未登录
      if (res.code === 401) {
        // 避免频繁弹窗，这里简单处理：直接清除 Token 并跳转登录
        MessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          localStorage.removeItem('Client-Token')
          router.push('/login')
        }).catch(() => {})
      } else if (res.code === 500) { // 500: 服务器错误
        Message({
          message: res.msg || '系统错误',
          type: 'error'
        })
      } else { // 其他业务错误
        Message({
          message: res.msg || '未知错误',
          type: 'error'
        })
      }

      return Promise.reject(new Error(res.msg || 'Error'))
    }
  },
  error => {
    console.log('err' + error)
    let { message } = error
    if (message === 'Network Error') {
      message = '后端接口连接异常'
    } else if (message.includes('timeout')) {
      message = '系统接口请求超时'
    } else if (message.includes('404')) {
      message = '404 接口不存在'
    }
    Message({
      message: message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service
