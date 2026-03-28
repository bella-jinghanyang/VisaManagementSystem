import request from '@/utils/request'

// 查询订单申请人基本信息列表
export function listApplicant(query) {
  return request({
    url: '/visa/applicant/list',
    method: 'get',
    params: query
  })
}

// 查询订单申请人基本信息详细
export function getApplicant(id) {
  return request({
    url: '/visa/applicant/' + id,
    method: 'get'
  })
}

// 新增订单申请人基本信息
export function addApplicant(data) {
  return request({
    url: '/visa/applicant',
    method: 'post',
    data: data
  })
}

// 修改订单申请人基本信息
export function updateApplicant(data) {
  return request({
    url: '/visa/applicant',
    method: 'put',
    data: data
  })
}

// 删除订单申请人基本信息
export function delApplicant(id) {
  return request({
    url: '/visa/applicant/' + id,
    method: 'delete'
  })
}
