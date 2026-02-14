import request from '@/utils/request'

// 加入预选清单
export function addToCart (data) {
  return request({
    url: '/client/cart/add',
    method: 'post',
    data: data
  })
}

// 获取清单列表
export function listCart (customerId) {
  return request({
    url: '/client/cart/list/' + customerId,
    method: 'get'
  })
}

// 获取清单数量 (用于角标)
export function getCartCount (customerId) {
  return request({
    url: '/client/cart/count/' + customerId,
    method: 'get'
  })
}

// 移除清单项
export function delCart (id) {
  return request({
    url: '/client/cart/' + id,
    method: 'delete'
  })
}

// 修改清单项数量
export function updateCartQuantity (id, quantity) {
  return request({
    url: '/client/cart/updateQuantity',
    method: 'put',
    data: { id, quantity }
  })
}
