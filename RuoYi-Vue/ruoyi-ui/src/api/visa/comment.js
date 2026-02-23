import request from '@/utils/request'

// 查询签证评价列表
export function listComment(query) {
  return request({
    url: '/visa/comment/list',
    method: 'get',
    params: query
  })
}

// 查询签证评价详细
export function getComment(id) {
  return request({
    url: '/visa/comment/' + id,
    method: 'get'
  })
}

// 新增签证评价
export function addComment(data) {
  return request({
    url: '/visa/comment',
    method: 'post',
    data: data
  })
}

// 修改签证评价
export function updateComment(data) {
  return request({
    url: '/visa/comment',
    method: 'put',
    data: data
  })
}

// 删除签证评价
export function delComment(id) {
  return request({
    url: '/visa/comment/' + id,
    method: 'delete'
  })
}
