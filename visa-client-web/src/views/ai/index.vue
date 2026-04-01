<template>
  <div class="ai-assistant-container container-width">
    <!-- 顶部状态栏：根据模式切换 -->
    <div class="ai-header">
      <div :class="['ai-badge', { 'human-mode': isHumanMode }]">
        {{ isHumanMode ? '人工客服已接入' : '智慧 AI 助手' }}
      </div>
      <h2 class="main-title">{{ isHumanMode ? '在线客服中心' : '签证咨询助手' }}</h2>
      <p class="sub-title">解答您的每一个出海疑问</p>
    </div>

    <!-- 聊天主窗口 -->
    <div class="chat-window">
      <div class="chat-content" ref="chatScroll">
        <div v-for="(msg, index) in messageList" :key="index" :class="['chat-group', msg.role]">

          <!-- 时间分割线 (可选) -->
          <div v-if="msg.showTime" class="time-divider">{{ msg.time }}</div>

          <div class="message-row">
            <!-- 头像 -->
            <div class="avatar-wrapper">
              <div class="avatar-inner">
                <i v-if="msg.role === 'assistant' && !isHumanMode" class="el-icon-cpu"></i>
                <i v-else-if="msg.role === 'assistant' && isHumanMode" class="el-icon-headset"></i>
                <i v-else class="el-icon-user"></i>
              </div>
            </div>

            <!-- 气泡 -->
            <div class="bubble-container">
              <div class="bubble">
                <!-- 加载动画 -->
                <div v-if="msg.loading" class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
                <!-- 内容渲染 -->
                <div v-else class="text-content" v-html="formatContent(msg.content)"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部输入区 (自带毛玻璃效果) -->
      <div class="input-section">
        <div class="input-inner">
          <input
            v-model="userInput"
            :placeholder="isHumanMode ? '请描述您的问题...' : '请描述您的问题...'"
            @keyup.enter="handleSend"
          />
          <el-button
            type="primary"
            icon="el-icon-position"
            circle
            class="send-btn"
            @click="handleSend"
            :loading="isGenerating"
          ></el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/* eslint-disable */
import { sendAiChat } from '@/api/ai';
import request from '@/utils/request';
import { marked } from 'marked';

export default {
  name: 'AiAssistant',

  data () {
    return {
      userInput: '',
      isGenerating: false,
      isHumanMode: false, // ★ 标记是否已切换到人工模式
      socket: null, // ★ WebSocket 对象
      user: JSON.parse(localStorage.getItem('Client-User')) || {},
      messageList: [
        {
          role: 'assistant',
          content: '您好！我是您的智慧签证助手。您可以问我关于各国签证的办理流程、费用以及材料要求。输入“人工”可召唤专员。',
          time: this.getCurrentTime()
        }
      ]
    }
  },
  created () {
    // 如果用户是从某个订单页面跳过来的，可以自动发一条招呼语
    const orderId = this.$route.query.orderId || 0
    // 1. 加载历史记录
    this.loadHistory(orderId)

    // 2. 监听
    this.$bus.$on('login-success', () => { this.loadHistory(orderId) })
    if (orderId) {
      this.messageList.push({ role: 'assistant', content: `收到您的订单咨询 (ID: ${orderId})，请问有什么可以帮您？`, time: this.getCurrentTime() })
    }
  },
  beforeDestroy () {
    // ★ 离开页面时断开连接
    if (this.socket) {
      this.socket.close()
    }
  },
  methods: {
    /** 核心发送逻辑 */
    handleSend() {
      const content = this.userInput.trim();
      if (!content || this.isGenerating) return;

      const user = JSON.parse(localStorage.getItem('Client-User')) || { id: 0 };
      const orderId = this.$route.query.orderId || 0;

      this.userInput = '';

      // 1. 检查是否切换人工
      if (!this.isHumanMode && (content === '人工' || content === '客服')) {
        this.initHumanMode();
        return;
      }

      // 2. 将消息显示在自己的界面
      this.messageList.push({ role: 'user', content: content, time: this.getCurrentTime() });
      this.scrollToBottom();

      // ★★★ 核心修复：根据模式严格分流 ★★★
      if (this.isHumanMode) {
        // --- 人工模式：只通过 WebSocket 发送 ---
        const msgObj = {
          toUserId: 'admin',
          fromUserId: user.id.toString(),
          content: content,
          orderId: parseInt(orderId)
        };
        this.socket.send(JSON.stringify(msgObj));
        // 这样这条消息才会出现在 B 端的实时窗口
      } else {
        // --- AI 模式：只调用 AI 接口 (HTTP) ---
        // 🔴 确保这里面千万没有 this.socket.send !!!
        this.sendViaAiApi(content, user.id, orderId);
      }
    },
    loadHistory(orderId) {
      const userStr = localStorage.getItem('Client-User');
      const user = userStr ? JSON.parse(userStr) : { id: 0 };

      // 调用接口，传入双重身份标识
      request.get('/client/ai/history', {
        params: {
          orderId: orderId,
          customerId: user.id
        }
      }).then(res => {
        if (res.data) {
          this.messageList = res.data.map(m => ({
            role: m.senderType === 1 ? 'user' : 'assistant',
            content: m.content,
            time: this.formatDate(m.createTime)
          }));
          this.scrollToBottom();
        }
      });
    },

    sendViaAiApi(content, customerId, orderId) {
      this.isGenerating = true;
      this.messageList.push({ role: 'assistant', content: '', loading: true, time: this.getCurrentTime() });
      this.scrollToBottom();

      // 调用接口，务必传 customerId 和 orderId 以便后端隔离数据
      // 注意：请确认你的 api/ai.js 接收这些参数
      sendAiChat(content, orderId, customerId).then(res => {
        const lastIndex = this.messageList.length - 1;
        this.messageList[lastIndex].loading = false;
        this.messageList[lastIndex].content = res.data;
        this.scrollToBottom();
      }).catch(err => {
        console.error("AI对话失败:", err);
        this.messageList.pop(); // 移除 loading 气泡
        this.$message.error('助手暂时无法连接，请重试');
      }).finally(() => {
        // ★ 无论成功还是失败，必须解锁，否则发不出第二句 ★
        this.isGenerating = false;
      });
    },
    /** 方案 B: WebSocket 人工逻辑 */
    initHumanMode () {
      this.isHumanMode = true
      this.messageList.push({ role: 'assistant', content: '正在为您分配签证专员，请稍候...', time: this.getCurrentTime() })

      // 建立连接 (假设后端地址是 localhost:8080)
      // 注意：这里的 URL 要对应后端 @ServerEndpoint("/websocket/chat/{userId}")
      const wsUrl = `ws://localhost:8080/websocket/chat/${this.user.id || 'guest'}`
      this.socket = new WebSocket(wsUrl)

      this.socket.onopen = () => {
        this.$message.success('人工客服已接入')
      }

      this.socket.onmessage = (event) => {
        const data = JSON.parse(event.data)
        // 接收来自管理员的消息
        this.messageList.push({
          role: 'assistant',
          content: data.content,
          time: this.getCurrentTime()
        })
        this.scrollToBottom()
      }

      this.socket.onerror = () => {
        this.$message.error('人工客服连接失败，请通过留言板联系')
        this.isHumanMode = false
      }
    },

    sendViaWebSocket (content) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        // ★★★ 核心修复：从路由参数中获取真正的 orderId ★★★
        const orderId = this.$route.query.orderId || 0

        const msgObj = {
          toUserId: 'admin',
          content: content,
          orderId: parseInt(orderId) // 确保是数字
        }
        this.socket.send(JSON.stringify(msgObj))
      }
    },
    loadHistory (orderId) {
      // 这里的接口建议你去后端 VisaOrderController 补一个通用的获取 message 的方法
      // 暂时用我们之前写的那个查询 order_message 的 Service
      request.get('/client/ai/history', { params: { orderId } }).then(res => {
        if (res.data) {
          this.messageList = res.data.map(m => ({
            role: m.senderType === 1 ? 'user' : 'assistant',
            content: m.content,
            time: this.formatDate(m.createTime)
          }))
          this.scrollToBottom()
        }
      })
    },

    /** 工具方法 */
    getCurrentTime () {
      const now = new Date()
      return now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0')
    },
    formatContent (content) {
      return content ? marked(content) : ''
    },
    scrollToBottom () {
      this.$nextTick(() => {
        const container = this.$refs.chatScroll
        if (container) container.scrollTop = container.scrollHeight
      })
    },
    /** 格式化历史消息时间 */
    formatDate (time) {
      if (!time) return ''
      // 如果你之前在 ProductDetail 里补过 parseTime，这里可以直接复用
      if (typeof this.parseTime === 'function') {
        return this.parseTime(time, '{h}:{i}')
      }

      // 如果没有 parseTime，用原生 JS 实现一个最简易的
      const date = new Date(time)
      const h = date.getHours().toString().padStart(2, '0')
      const i = date.getMinutes().toString().padStart(2, '0')
      return `${h}:${i}`
    }
  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

/* 1. 页面整体布局 */
.ai-assistant-container {
  padding: 40px 0;
  height: 85vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ai-header {
  text-align: center;
  margin-bottom: 30px;

  .ai-badge {
    display: inline-block;
    background: rgba(106, 175, 230, 0.1);
    color: $primary-color;
    padding: 4px 14px;
    border-radius: 50px;
    font-size: 12px;
    font-weight: 700;
    margin-bottom: 15px;
    transition: all 0.3s;

    &.human-mode {
      background: #E1F3D8;
      color: #67C23A;
    }
  }
  .main-title { font-size: 28px; color: $text-main; font-weight: 800; margin-bottom: 8px; }
  .sub-title { font-size: 14px; color: #999; }
}

/* 2. 聊天窗口容器 */
.chat-window {
  width: 100%;
  max-width: 900px;
  flex: 1;
  background: #ffffff;
  border-radius: 32px; // 超大圆角
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.chat-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background-color: #FAFBFC;
}

/* 3. 消息行布局 */
.chat-group {
  margin-bottom: 25px;
  display: flex;
  flex-direction: column;
}

.message-row {
  display: flex;
  align-items: flex-start;
}

.avatar-wrapper {
  flex-shrink: 0;
  .avatar-inner {
    width: 40px; height: 40px;
    border-radius: 12px;
    display: flex; align-items: center; justify-content: center;
    font-size: 20px;
  }
}

.bubble-container {
  max-width: 75%;
}

.bubble {
  padding: 14px 18px;
  font-size: 15px;
  line-height: 1.6;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

/* 4. AI 消息样式 (左侧) */
.assistant {
  .message-row { flex-direction: row; }
  .avatar-inner { background: #fff; color: $primary-color; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
  .bubble-container { margin-left: 12px; }
  .bubble {
    background: #FFFFFF;
    color: $text-main;
    border-radius: 4px 18px 18px 18px; // 气泡小尾巴在左上
    border: 1px solid #edf2f7;
    text-align: left !important;
  }
}

/* 5. 用户消息样式 (右侧) */
.user {
  .message-row { flex-direction: row-reverse; }
  .avatar-inner { background: $primary-color; color: #fff; box-shadow: 0 4px 12px rgba(106, 175, 230, 0.3); }
  .bubble-container { margin-right: 12px; }
  .bubble {
    background: linear-gradient(135deg, $primary-color 0%, #8CC5F3 100%);
    color: #fff;
    border-radius: 18px 4px 18px 18px; // 气泡小尾巴在右上
    text-align: left !important;
  }
}

/* 6. 底部输入区 */
.input-section {
  padding: 20px 30px 30px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-top: 1px solid #f0f2f5;

  .input-inner {
    background: #F2F4F7;
    border-radius: 20px;
    padding: 6px 6px 6px 20px;
    display: flex;
    align-items: center;
    transition: all 0.3s;

    &:focus-within {
      background: #fff;
      box-shadow: 0 0 0 3px rgba(106, 175, 230, 0.15);
      border: 1px solid $primary-color;
    }

    input {
      flex: 1;
      border: none;
      background: transparent;
      outline: none;
      font-size: 15px;
      color: $text-main;
    }

    .send-btn {
      width: 40px; height: 40px;
      background: $primary-color;
      border: none;
      font-size: 18px;
      box-shadow: 0 4px 10px rgba(106, 175, 230, 0.3);
    }
  }
}

/* 打字机加载动画 */
.typing-indicator {
  display: flex; gap: 4px; padding: 6px 0;
  span {
    width: 6px; height: 6px; background: $primary-color;
    border-radius: 50%; opacity: 0.3;
    animation: bounce 1.2s infinite;
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); opacity: 1; } }

/* Markdown 样式修饰 */
.text-content {
  ::v-deep p { margin: 0; }
  ::v-deep ul, ::v-deep ol { padding-left: 20px; margin: 8px 0; }
}
</style>
