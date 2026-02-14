import request from '@/utils/request'

// 注册
export function register (data) {
  return request({
    url: '/client/auth/register',
    method: 'post',
    data: data
  })
}

// 登录
export function login (data) {
  return request({
    url: '/client/auth/login',
    method: 'post',
    data: data
  })
}
