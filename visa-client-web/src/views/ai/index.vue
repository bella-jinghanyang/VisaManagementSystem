<template>
  <div class="ai-assistant-container container-width">
    <!-- 头部标识 -->
    <div class="ai-header">
      <div class="ai-badge">AI</div>
      <h2>智慧签证助手</h2>
      <p>基于 RAG 技术，为您提供精准的签证政策咨询</p>
    </div>

    <!-- 聊天主窗口 -->
    <apple-card class="chat-window">
      <div class="chat-content" ref="chatScroll">
        <!-- 循环渲染消息 -->
        <div v-for="(msg, index) in messageList" :key="index" :class="['chat-item', msg.role]">
          <!-- 头像区 -->
          <div class="avatar">
            <i :class="msg.role === 'assistant' ? 'el-icon-cpu' : 'el-icon-user'"></i>
          </div>
          <!-- 消息气泡 -->
          <div class="bubble-wrapper">
            <div class="bubble">
              <div v-if="msg.loading" class="typing-loader">
                <span></span><span></span><span></span>
              </div>
              <div v-else class="text-content" v-html="formatContent(msg.content)"></div>
            </div>
            <div class="msg-time">{{ msg.time }}</div>
          </div>
        </div>
      </div>

      <!-- 底部输入区 -->
      <div class="input-section">
        <div class="input-wrapper">
          <input
            v-model="userInput"
            placeholder="问问我：办日本签证需要多少钱？"
            @keyup.enter="handleSend"
            :disabled="isGenerating"
          />
          <el-button
            type="primary"
            icon="el-icon-position"
            circle
            @click="handleSend"
            :loading="isGenerating"
          ></el-button>
        </div>
      </div>
    </apple-card>
  </div>
</template>

<script>
import AppleCard from '@/components/AppleCard'
import { sendAiChat } from '@/api/ai'
import dayjs from 'dayjs'
import { marked } from 'marked'

export default {
  name: 'AiAssistant',
  components: { AppleCard },
  data () {
    return {
      userInput: '',
      isGenerating: false,
      messageList: [
        {
          role: 'assistant',
          content: '您好！我是您的智慧签证助手。您可以问我关于各国签证的办理流程、费用以及材料要求。',
          time: dayjs().format('HH:mm')
        }
      ]
    }
  },
  methods: {
    /** 发送消息 */
    handleSend () {
      if (!this.userInput.trim() || this.isGenerating) return

      const userMsg = this.userInput
      this.userInput = ''

      // 1. 添加用户消息到列表
      this.messageList.push({
        role: 'user',
        content: userMsg,
        time: dayjs().format('HH:mm')
      })

      // 2. 添加一个 AI 的“加载中”占位气泡
      this.isGenerating = true
      this.messageList.push({
        role: 'assistant',
        content: '',
        loading: true,
        time: dayjs().format('HH:mm')
      })

      this.scrollToBottom()

      // 3. 调用后端 RAG 接口
      sendAiChat(userMsg).then(res => {
        // 移除加载状态，填入真实内容
        const lastIndex = this.messageList.length - 1
        this.messageList[lastIndex].loading = false
        this.messageList[lastIndex].content = res.data
        this.scrollToBottom()
      }).catch(() => {
        this.messageList.pop()
        this.$message.error('连接大脑失败，请稍后再试')
      }).finally(() => {
        this.isGenerating = false
      })
    },

    /** 内容格式化（简单处理换行） */
    formatContent (content) {
      if (!content) return ''
      return marked(content) // 将 Markdown 转为 HTML
    },

    /** 自动滚动到底部 */
    scrollToBottom () {
      this.$nextTick(() => {
        const container = this.$refs.chatScroll
        container.scrollTo({
          top: container.scrollHeight,
          behavior: 'smooth'
        })
      })
    }

  }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

.ai-assistant-container {
  padding: 40px 0;
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.ai-header {
  text-align: center;
  margin-bottom: 30px;
  .ai-badge {
    display: inline-block;
    background: $primary-color;
    color: #fff;
    padding: 2px 10px;
    border-radius: 8px;
    font-weight: 800;
    font-size: 12px;
    margin-bottom: 10px;
  }
  h2 { font-weight: 700; color: $text-main; }
  p { color: $text-secondary; font-size: 14px; }
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 !important;
  overflow: hidden;
  height: 600px;
}

.chat-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background-color: #fafafa;
}

/* 气泡基础样式 */
.chat-item {
  display: flex;
  margin-bottom: 25px;

  .avatar {
    width: 40px; height: 40px;
    border-radius: 12px;
    display: flex; align-items: center; justify-content: center;
    font-size: 20px;
    flex-shrink: 0;
  }

  .bubble-wrapper {
    max-width: 70%;
    margin: 0 15px;
  }

  .bubble {
    padding: 15px 20px;
    border-radius: 18px;
    font-size: 15px;
    line-height: 1.6;
    box-shadow: 0 2px 10px rgba(0,0,0,0.03);

    text-align: left !important;
    word-break: break-word; // 确保长句子换行
  }

  .msg-time {
    font-size: 11px;
    color: #ccc;
    margin-top: 5px;
  }
}

/* AI 的气泡 (左侧) */
.assistant {
  .avatar { background: #EBF5FF; color: $primary-color; }
  .bubble {
    background-color: #fff;
    color: $text-main;
    border-bottom-left-radius: 4px;
    border: 1px solid #eee;
  }
  .msg-time { text-align: left; }
}

/* 用户的气泡 (右侧) */
.user {
  flex-direction: row-reverse;
  .avatar { background: $primary-color; color: #fff; }
  .bubble {
    background: linear-gradient(135deg, $primary-color 0%, $primary-hover 100%);
    color: #fff;
    border-bottom-right-radius: 4px;
  }
  .msg-time { text-align: right; }
}

/* 底部输入区 */
.input-section {
  padding: 20px 30px;
  background: #fff;
  border-top: 1px solid #f0f0f0;

  .input-wrapper {
    background: #f5f5f7;
    border-radius: 30px;
    padding: 5px 5px 5px 20px;
    display: flex;
    align-items: center;
    border: 1px solid transparent;
    transition: all 0.3s;

    &:focus-within {
      background: #fff;
      border-color: $primary-color;
      box-shadow: 0 0 0 4px rgba(106, 175, 230, 0.1);
    }

    input {
      flex: 1;
      border: none;
      background: transparent;
      outline: none;
      height: 40px;
      font-size: 15px;
    }
  }
}

/* 打字机动画 */
.typing-loader {
  display: flex; gap: 4px; padding: 5px 0;
  span {
    width: 8px; height: 8px; background: $primary-color;
    border-radius: 50%; opacity: 0.3;
    animation: typing 1s infinite;
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}
@keyframes typing {
  0%, 100% { transform: translateY(0); opacity: 0.3; }
  50% { transform: translateY(-5px); opacity: 1; }
}
</style>
