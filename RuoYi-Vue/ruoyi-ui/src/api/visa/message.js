import request from '@/utils/request'

// 查询订单留言记录列表
export function listMessage(query) {
  return request({
    url: '/visa/message/list',
    method: 'get',
    params: query
  })
}

// 查询订单留言记录详细
export function getMessage(id) {
  return request({
    url: '/visa/message/' + id,
    method: 'get'
  })
}

// 新增订单留言记录
export function addMessage(data) {
  return request({
    url: '/visa/message',
    method: 'post',
    data: data
  })
}

// 修改订单留言记录
export function updateMessage(data) {
  return request({
    url: '/visa/message',
    method: 'put',
    data: data
  })
}

// 删除订单留言记录
export function delMessage(id) {
  return request({
    url: '/visa/message/' + id,
    method: 'delete'
  })
}
