import request from '@/utils/request'

// 获取国家列表
export function listCountry () {
  return request({
    url: '/client/product/country',
    method: 'get'
  })
}

// 获取签证类型列表
export function listType () {
  return request({
    url: '/client/product/type',
    method: 'get'
  })
}
