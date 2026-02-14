import request from '@/utils/request'

// 提交订单
export function submitOrder (data) {
  return request({
    url: '/client/order/submit',
    method: 'post',
    data: data
  })
}

// 获取我的订单列表 (先占位)
export function listMyOrders (customerId) {
  return request({
    url: '/client/order/list/' + customerId,
    method: 'get'
  })
}

// 获取待办订单数量
export function getPendingOrderCount (customerId) {
  return request({
    url: '/client/order/countPending/' + customerId,
    method: 'get'
  })
}

// 提交材料
export function submitMaterials (data) {
  return request({
    url: '/client/order/submitMaterials',
    method: 'post',
    data: data
  })
}

// 获取单个订单详情
export function getOrder (id) {
  return request({
    url: '/client/order/detail/' + id,
    method: 'get'
  })
}
