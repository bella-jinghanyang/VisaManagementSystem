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

// 上传原始文档（PDF / Word / Excel 等）并自动完成知识摄取
export function uploadKnowledgeDoc(formData) {
  return request({
    url: '/visa/knowledge/upload-doc',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 批量刷新所有有效知识条目的语义向量，重建 Elasticsearch 索引
export function refreshEmbeddings() {
  return request({
    url: '/visa/knowledge/refresh_all',
    method: 'post'
  })
}
