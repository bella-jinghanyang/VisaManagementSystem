import request from '@/utils/request'

// 获取产品评价
export function getProductComments (productId) {
  return request({
    url: '/client/comment/list/' + productId,
    method: 'get'
  })
}

// 提交评价
export function addComment (data) {
  return request({
    url: '/client/comment/add',
    method: 'post',
    data: data
  })
}

// 追加评价
export function addAdditionalComment (data) {
  return request({
    url: '/client/comment/addAdditional',
    method: 'put',
    data: data
  })
}
