import request from '@/utils/request'

// 查询C端产品列表
export function listProduct (query) {
  return request({
    url: '/client/product/list', // 对应后端 ClientProductController 的接口
    method: 'get',
    params: query
  })
}

// 查询产品详情 (后面做详情页也要用)
export function getProduct (id) {
  return request({
    url: '/client/product/' + id,
    method: 'get'
  })
}
