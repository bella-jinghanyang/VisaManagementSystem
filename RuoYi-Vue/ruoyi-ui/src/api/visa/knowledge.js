import request from '@/utils/request'

// 查询签证知识库列表
export function listKnowledge(query) {
  return request({
    url: '/visa/knowledge/list',
    method: 'get',
    params: query
  })
}

// 查询签证知识库详细
export function getKnowledge(id) {
  return request({
    url: '/visa/knowledge/' + id,
    method: 'get'
  })
}

// 新增签证知识库
export function addKnowledge(data) {
  return request({
    url: '/visa/knowledge',
    method: 'post',
    data: data
  })
}

// 修改签证知识库
export function updateKnowledge(data) {
  return request({
    url: '/visa/knowledge',
    method: 'put',
    data: data
  })
}

// 删除签证知识库
export function delKnowledge(id) {
  return request({
    url: '/visa/knowledge/' + id,
    method: 'delete'
  })
}
