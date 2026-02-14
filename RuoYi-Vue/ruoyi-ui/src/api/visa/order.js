import request from '@/utils/request'

// 查询签证订单列表
export function listOrder(query) {
  return request({
    url: '/visa/order/list',
    method: 'get',
    params: query
  })
}

// 查询签证订单详细
export function getOrder(id) {
  return request({
    url: '/visa/order/' + id,
    method: 'get'
  })
}

// 新增签证订单
export function addOrder(data) {
  return request({
    url: '/visa/order',
    method: 'post',
    data: data
  })
}

// 修改签证订单
export function updateOrder(data) {
  return request({
    url: '/visa/order',
    method: 'put',
    data: data
  })
}

// 删除签证订单
export function delOrder(id) {
  return request({
    url: '/visa/order/' + id,
    method: 'delete'
  })
}
