import request from '@/utils/request'

// 发送 AI 对话请求
export function sendAiChat (message, orderId, customerId) {
  return request({
    url: '/client/ai/chat',
    method: 'get',
    params: {
      q: message,
      orderId: orderId,
      customerId: customerId // 新增：传给后端存库
    }
  })
}

export function getChatHistory (orderId) {
  return request({
    url: '/client/ai/history', // 对应你后端写的接口
    method: 'get',
    params: { orderId: orderId || 0 }
  })
}
