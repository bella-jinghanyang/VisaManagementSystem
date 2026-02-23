import request from '@/utils/request'

// 发送 AI 对话请求
export function sendAiChat (message) {
  return request({
    url: '/client/ai/chat',
    method: 'get',
    params: { q: message }
  })
}
