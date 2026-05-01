<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧：咨询列表 -->
      <el-col :span="6">
        <el-card class="box-card" shadow="never">
          <div slot="header">
            <span>正在咨询的客户</span>
          </div>
          <div class="user-list">
            <div v-for="user in userList" :key="user.combinedId"
              :class="['user-item', { active: currentSessionId === user.combinedId }]" @click="handleSelectUser(user)">

              <el-badge :is-dot="user.unread" class="item">
                <el-avatar icon="el-icon-user-solid"></el-avatar>
              </el-badge>
              <div class="user-info">
                <!-- 第一行：显示客户名 -->
                <div class="username">{{ user.nickname }}</div>
                <!-- 第二行：显示关联的订单号 -->
                <div class="order-tag" v-if="user.orderNo">
                  <el-tag size="mini" type="warning">单: {{ user.orderNo }}</el-tag>
                </div>
                <div class="last-msg">{{ user.lastMsg }}</div>
              </div>
            </div>
            <div v-if="userList.length === 0" class="empty-text">暂无在线咨询</div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：聊天窗口 -->
      <el-col :span="18">
        <el-card class="box-card chat-card" shadow="never" v-if="currentUserId !== null">
          <div slot="header">
            <span>沟通中：{{ currentUserName }}</span>
          </div>

          <div class="order-context-bar" v-if="currentOrderId > 0">
            <div class="context-info">
              <el-tag size="small" type="warning">咨询订单</el-tag>
              <span class="order-no">单号：{{ currentOrderNo }}</span>
              <el-button type="text" size="mini" @click="handleViewOrder">查看订单详情</el-button>
            </div>
          </div>

          <!-- 消息展示区 -->
          <div class="message-box" ref="messageBox">
            <div v-for="(msg, index) in messageList" :key="index"
              :class="['msg-row', msg.senderType === 2 ? 'msg-me' : 'msg-other']">
              <el-avatar :size="30" :icon="msg.senderType === 2 ? 'el-icon-headset' : 'el-icon-user'"></el-avatar>
              <div class="msg-content">
                {{ msg.content }}
              </div>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="input-area">
            <el-input type="textarea" :rows="4" placeholder="请输入回复内容，按 Ctrl + Enter 发送" v-model="inputText"
              @keyup.native.ctrl.enter="handleSend"></el-input>
            <div class="send-btn">
              <el-button type="primary" size="small" @click="handleSend">发 送</el-button>
            </div>
          </div>
        </el-card>

        <div v-else class="welcome-box">
          <i class="el-icon-chat-dot-round" style="font-size: 80px; color: #E4E7ED;"></i>
          <p>请在左侧选择客户开始沟通</p>
        </div>
      </el-col>
    </el-row>

    <el-dialog title="关联订单详情" :visible.sync="orderDetailOpen" width="800px" append-to-body>
      <div v-if="orderDetail.id">
        <!-- 1. 订单状态概览 -->
        <div style="margin-bottom: 20px;">
          <el-tag :type="getStatusType(orderDetail.status)">状态：{{ statusMap[orderDetail.status] }}</el-tag>
          <span style="margin-left: 20px; color: #666;">订单号：{{ orderDetail.orderNo }}</span>
        </div>

        <!-- 2. 复用之前的申请人材料展示逻辑 -->
        <div v-for="(person, idx) in parseMaterials(orderDetail.submittedMaterials)" :key="idx" class="person-box"
          style="margin-bottom: 20px; border: 1px solid #eee; padding: 15px; border-radius: 8px;">
          <div style="font-weight: bold; margin-bottom: 10px; color: #409EFF; font-size: 15px;">
            <i class="el-icon-user"></i>
            {{ (orderDetail.applicantList && orderDetail.applicantList[idx]) ? orderDetail.applicantList[idx].name :
              '申请人 ' + (idx + 1) }}

            <!-- 顺便显示一下身份标签，更专业 -->
            <el-tag v-if="orderDetail.applicantList && orderDetail.applicantList[idx]" size="mini" type="info"
              effect="plain" style="margin-left: 10px; border:none;">
              {{ getIdentityLabel(orderDetail.applicantList[idx].identity) }}
            </el-tag>
          </div>
          <el-descriptions border :column="1" size="small">
            <el-descriptions-item v-for="(val, key) in person" :key="key" :label="key">
              <!-- 1. 如果是图片：直接显示缩略图 -->
              <div v-if="isImage(val)">
                <el-image :src="getUrl(val)" :preview-src-list="[getUrl(val)]"
                  style="width: 100px; height: 70px; border-radius: 4px; border: 1px solid #eee; cursor: zoom-in;">
                </el-image>
              </div>

              <!-- 2. ★ 新增：如果是文件路径 (如 PDF, Doc) ★ -->
              <!-- 逻辑：如果不是图片，但包含 /profile/upload 路径，则渲染为可点击的链接 -->
              <div v-else-if="val && val.toString().indexOf('/profile/upload') > -1">
                <el-link type="primary" :underline="false" @click="previewFile(val)">
                  <i class="el-icon-document"></i> 预览/下载文件
                </el-link>
              </div>

              <!-- 3. 如果是普通文字：直接显示 -->
              <div v-else>{{ val || '-' }}</div>
            </el-descriptions-item>
          </el-descriptions>

          <!-- 如果有补件，也显示出来 -->
          <div v-if="getPersonSuppFiles(orderDetail, idx).length > 0"
            style="margin-top: 10px; background: #fdf6ec; padding: 10px;">
            <div style="font-size: 12px; color: #e6a23c; margin-bottom: 5px;">补交材料：</div>
            <el-link v-for="file in getPersonSuppFiles(orderDetail, idx)" :key="file" type="primary"
              @click="previewFile(file)" style="margin-right:10px">查看附件</el-link>
          </div>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="orderDetailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
/* eslint-disable */
import request from '@/utils/request';

export default {
  name: "VisaChat",
  data() {
    return {
      socket: null,
      userList: [],
      currentSessionId: null,
      currentUserName: '',
      messageList: [],
      inputText: '',
      adminId: 'admin',

      // ★★★ 必须显式初始化这些变量，解决模板渲染报错 ★★★
      currentUserId: null,
      currentOrderId: 0,
      currentOrderNo: '',

      orderDetailOpen: false,
      orderDetail: {},
      statusMap: {
        0: '待支付', 1: '待上传', 2: '审核中', 3: '需补交',
        4: '办理中', 5: '待收货', 6: '已完成'
      },

      // 其他辅助
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      headers: { Authorization: "Bearer " + localStorage.getItem('Admin-Token') },
      applicantListInDialog: [],
      // WebSocket 自动重连定时器引用
      reconnectTimer: null
    };
  },
  async mounted() {
    // 1. 【核心修改】先连接 WebSocket，保证“管道”第一时间通畅
    this.initWebSocket();

    // 2. 加载用户列表
    await this.loadChatUsers();

    // 3. 处理从“订单管理”主动跳转过来的逻辑
    const q = this.$route.query;
    if (q.customerId && q.orderId) {
      console.log("检测到主动联系请求:", q);
      const combinedId = q.customerId + '-' + q.orderId;
      const existUser = this.userList.find(u => u.combinedId === combinedId);

      if (existUser) {
        this.handleSelectUser(existUser);
      } else {
        // 列表里没有，手动创建一个“虚拟节点”塞进去，让客服能直接发消息
        const tempUser = {
          id: parseInt(q.customerId),
          orderId: parseInt(q.orderId),
          orderNo: q.orderNo,
          nickname: q.nickname || ('客户 ' + q.customerId),
          lastMsg: '--- 准备发起新沟通 ---',
          combinedId: combinedId,
          unread: false
        };
        this.userList.unshift(tempUser);
        this.handleSelectUser(tempUser);
      }
    }
  },
  beforeDestroy() {
    // 先清除重连定时器，确保 close 触发的 onclose 不会再次重连
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    if (this.socket) {
      this.socket.onclose = null;
      this.socket.close();
    }
  },
  methods: {
    initWebSocket() {
      // 清除上一次的自动重连定时器
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer);
        this.reconnectTimer = null;
      }

      // 关闭旧连接（先摘除 onclose，防止触发重连循环）
      if (this.socket && this.socket.readyState !== WebSocket.CLOSED) {
        this.socket.onclose = null;
        this.socket.close();
      }

      // 优先使用环境变量；若未配置则直连后端 8080（与用户端行为一致，绕过不稳定的 dev 代理）
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsBase = process.env.VUE_APP_WS_URL || `${wsProtocol}//localhost:8080`;
      const wsUrl = `${wsBase}/websocket/chat/admin`;
      this.socket = new WebSocket(wsUrl);

      this.socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        this.handleReceivedMessage(data);
      };

      this.socket.onopen = () => {
        console.log(">>> 客服工作台连接成功 (admin)");
      };

      this.socket.onclose = () => {
        console.warn("<<< WebSocket 连接已断开，3 秒后自动重连...");
        // clearTimeout 可确保 beforeDestroy 清除定时器后回调不会再执行
        this.reconnectTimer = setTimeout(() => {
          this.initWebSocket();
        }, 3000);
      };

      this.socket.onerror = () => {
        this.$message.error("聊天服务暂时不可用，请稍后重试");
      };
    },

    loadChatUsers() {
      // ★ 必须返回 Promise 以便 mounted 使用 await ★
      return request({ url: '/visa/order/chat/users', method: 'get' }).then(res => {
        this.userList = (res.rows || []).map(item => ({
          id: item.customerId,
          orderId: item.id,
          orderNo: item.orderNo,
          nickname: item.customerName || ('客户' + item.customerId),
          lastMsg: item.lastMsg,
          combinedId: item.customerId + '-' + item.id,
          unread: false
        }));
      }).catch(err => {
        console.error("加载咨询列表失败:", err);
      });
    },

    /** 选择用户开始聊天 */
    handleSelectUser(user) {
      console.log("当前选中会话:", user);
      this.currentSessionId = user.combinedId;
      this.currentUserId = user.id;
      this.currentUserName = user.nickname;
      this.currentOrderId = user.orderId;
      this.currentOrderNo = user.orderNo;
      user.unread = false;

      // 加载历史记录
      request({
        url: '/visa/order/chat/history',
        method: 'get',
        params: {
          orderId: user.orderId,
          customerId: user.id,
          isAi: '0'
        }
      }).then(res => {
        this.messageList = res.data || [];
        this.scrollToBottom();
      });
    },

    /** 发送消息 */
    handleSend() {
      if (!this.inputText.trim()) return;
      if (!this.currentUserId) return this.$message.warning("请先选择一个客户");

      // 检查连接
      if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
        this.$message.error("聊天已断开，正在尝试重连...");
        this.initWebSocket();
        return;
      }

      const msgObj = {
        toUserId: this.currentUserId.toString(),
        fromUserId: 'admin',
        content: this.inputText,
        orderId: this.currentOrderId || 0, // 核心：绑定订单ID
        senderType: 2
      };

      this.socket.send(JSON.stringify(msgObj));

      // 预览
      this.messageList.push({
        senderType: 2,
        content: this.inputText,
        createTime: new Date()
      });

      this.inputText = '';
      this.scrollToBottom();
    },

    /** 处理收到的消息 */
    handleReceivedMessage(data) {
      console.log("收到新消息:", data);
      // 如果消息是当前正在沟通的人发的
      if (data.fromUserId == this.currentUserId) {
        this.messageList.push({
          senderType: 1,
          content: data.content
        });
        this.scrollToBottom();
      } else {
        // 否则刷新左侧列表
        this.loadChatUsers();
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const box = this.$refs.messageBox;
        if (box) box.scrollTop = box.scrollHeight;
      });
    },

    // 订单详情预览相关逻辑（保持你原来的 getUrl, handleViewOrder 等方法即可）
    handleViewOrder() {
      if (!this.currentOrderId) return;
      request({ url: '/visa/order/' + this.currentOrderId, method: 'get' }).then(res => {
        this.orderDetail = res.data;
        this.orderDetailOpen = true;
      });
    },
    // ... 其他 getIdentityLabel, parseMaterials 等辅助方法 ...
    getIdentityLabel(id) {
      const map = { 'EMPLOYED': '在职', 'STUDENT': '学生', 'CHILD': '学龄前', 'RETIRED': '退休' };
      return map[id] || '客户';
    },
    getUrl(path) {
      return path && path.startsWith('http') ? path : (process.env.VUE_APP_BASE_API + path);
    },
    isImage(path) {
      return /\.(jpg|jpeg|png|gif)$/i.test(path);
    },
    parseMaterials(json) {
      try { return JSON.parse(json || '[]'); } catch (e) { return []; }
    },
    getPersonSuppFiles(order, index) {
      if (!order.supplementaryMaterials) return [];
      try {
        const data = JSON.parse(order.supplementaryMaterials);
        return data[index.toString()] || [];
      } catch (e) { return []; }
    },
    getStatusType(status) {
      const map = { 0: 'info', 1: 'primary', 2: 'warning', 4: 'success' };
      return map[status] || 'info';
    },
    previewFile(url) {
      window.open(this.getUrl(url), '_blank');
    }
  }
};
</script>

<style scoped lang="scss">
.user-list {
  height: 600px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 15px;
  cursor: pointer;
  border-bottom: 1px solid #f5f7fa;

  &:hover {
    background: #fcfcfc;
  }

  &.active {
    background: #f0f7ff;
  }

  .user-info {
    margin-left: 12px;
    overflow: hidden;

    .username {
      font-weight: bold;
      font-size: 14px;
    }

    .last-msg {
      font-size: 12px;
      color: #999;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
    }
  }
}

.message-box {
  height: 400px;
  overflow-y: auto;
  padding: 20px;
  background: #f9f9f9;
  border: 1px solid #eeeeee;
  border-radius: 4px;
}

.msg-row {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.msg-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 4px;
  font-size: 14px;
  margin: 0 10px;
  line-height: 1.5;
}

/* 别人发的消息 (左侧) */
.msg-other {
  flex-direction: row;

  .msg-content {
    background: #fff;
    border: 1px solid #e4e7ed;
  }
}

/* 我发的消息 (右侧) */
.msg-me {
  flex-direction: row-reverse;

  .msg-content {
    background: #1890ff;
    color: #fff;
  }
}

.input-area {
  margin-top: 20px;

  .send-btn {
    text-align: right;
    margin-top: 10px;
  }
}

.welcome-box {
  height: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.empty-text {
  text-align: center;
  color: #999;
  padding-top: 50px;
}

.user-item {
  height: auto !important; // 允许内容撑开
  padding: 12px 15px;

  .user-info {
    .username {
      margin-bottom: 5px;
    }

    .order-tag {
      margin-bottom: 5px;
    }

    .last-msg {
      font-size: 11px;
    }
  }
}
</style>