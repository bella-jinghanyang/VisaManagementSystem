import request from '@/utils/request'

// 获取个人信息
export function getUserProfile (id) {
  return request({
    url: '/client/user/profile/' + id,
    method: 'get'
  })
}

// 修改个人信息
export function updateUserProfile (data) {
  return request({
    url: '/client/user/profile',
    method: 'put',
    data: data
  })
}
