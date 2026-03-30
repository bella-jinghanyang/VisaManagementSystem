import request from '@/utils/request'

// 查询订单物流信息列表
export function listLogistics(query) {
  return request({
    url: '/visa/logistics/list',
    method: 'get',
    params: query
  })
}

// 查询订单物流信息详细
export function getLogistics(id) {
  return request({
    url: '/visa/logistics/' + id,
    method: 'get'
  })
}

// 新增订单物流信息
export function addLogistics(data) {
  return request({
    url: '/visa/logistics',
    method: 'post',
    data: data
  })
}

// 修改订单物流信息
export function updateLogistics(data) {
  return request({
    url: '/visa/logistics',
    method: 'put',
    data: data
  })
}

// 删除订单物流信息
export function delLogistics(id) {
  return request({
    url: '/visa/logistics/' + id,
    method: 'delete'
  })
}
