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
                <!-- ★ 新增第二行：显示关联的订单号 ★ -->
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

    <!-- 在 chat/index.vue 的最后面添加 -->

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
          <div style="font-weight: bold; margin-bottom: 10px; color: #409EFF;">申请人 {{ idx + 1 }}</div>
          <el-descriptions border :column="1" size="small">
            <el-descriptions-item v-for="(val, key) in person" :key="key" :label="key">
              <div v-if="isImage(val)">
                <el-image :src="getUrl(val)" :preview-src-list="[getUrl(val)]"
                  style="width: 100px; height: 70px"></el-image>
              </div>
              <div v-else>{{ val }}</div>
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
import request from '@/utils/request';

export default {
  name: "VisaChat",
  data() {
    return {
      socket: null,
      userList: [], // 模拟正在聊天的用户列表
      currentSessionId: null,
      currentUserName: '',
      messageList: [],
      inputText: '',
      // 管理员 ID 写死为 admin
      adminId: 'admin',
      orderDetailOpen: false,
      orderDetail: {}, // 存放查出来的订单详情
      orderDetailOpen: false,
      orderDetail: {},
      // 新增状态映射
      statusMap: {
        0: '待支付',
        1: '已支付',
        2: '资料审核中',
        3: '办理中',
        4: '已完成',
        5: '已退款'
      }

    };
  },
  mounted() {
    this.initWebSocket();
    this.loadChatUsers();
    const q = this.$route.query;
    if (q.customerId) {
      this.handleSelectUser({
        id: q.customerId,
        orderId: q.orderId,
        combinedId: q.customerId + '-' + q.orderId,
        nickname: '客户 ' + q.customerId,
        orderNo: q.orderNo
      });
    }
  },
  beforeDestroy() {
    if (this.socket) this.socket.close();
  },
  methods: {
    handleSelectUser(user) {
      this.currentSessionId = user.combinedId;
      this.currentUserId = user.customerId;
      this.currentUserName = user.customerName;
      this.currentOrderId = user.orderId;
      this.currentOrderNo = user.orderNo; // 从 SQL 查出来的单号

      // 加载历史记录时带上双重过滤
      request({
        url: '/visa/order/chat/history',
        params: {
          orderId: user.orderId,
          customerId: user.customerId,
          isAi: '0'
        }
      }).then(res => {
        this.messageList = res.data;
      });
    },
    initWebSocket() {
      // 连接地址对应后端的 @ServerEndpoint("/websocket/chat/{userId}")
      const wsUrl = "ws://127.0.0.1:8080/websocket/chat/admin";
      this.socket = new WebSocket(wsUrl);

      this.socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        this.handleReceivedMessage(data);
      };

      this.socket.onopen = () => {
        console.log("客服工作台已上线");
      };
    },

    /** 处理收到的消息 */
    handleReceivedMessage(data) {
      // ★ 1. 看看收到的 JSON 到底长什么样 ★
      console.log("B端收到原始数据包:", data);

      // 2. 检查是否有 fromUserId
      if (!data.fromUserId) {
        console.error("【严重错误】收到消息但没有 fromUserId，无法归类！请检查后端代码。");
        return;
      }
      // 3. 如果收到的消息正是当前正在聊天的用户
      if (data.fromUserId == this.currentUserId) {
        this.messageList.push({
          senderType: 1, // 客户
          content: data.content
        });
        this.scrollToBottom();
      } else {
        // 4. 如果是其他用户，更新左侧列表状态
        this.updateUserList(data);
      }
    },

    updateUserList(data) {
      console.log("准备更新左侧列表，发送者 ID:", data.fromUserId);

      // 查找列表里是否已存在该用户
      // 注意：用 == 比较，防止字符串 "1" 和数字 1 匹配不上
      const index = this.userList.findIndex(u => u.id == data.fromUserId);

      if (index !== -1) {
        // 情况 A：用户已在列表，更新消息并置顶
        const user = this.userList[index];
        user.lastMsg = data.content;
        user.unread = true;

        // 删掉旧的，重新塞到最前面（实现置顶效果，且触发 Vue 刷新）
        this.userList.splice(index, 1);
        this.userList.unshift(user);
      } else {
        // 情况 B：新用户咨询，直接塞到最前面
        this.userList.unshift({
          id: data.fromUserId,
          nickname: '客户 ' + data.fromUserId,
          lastMsg: data.content,
          unread: true
        });
      }

      // ★ 核心加固：强制告诉 Vue 列表变了
      this.userList = [...this.userList];
      console.log("当前列表总人数:", this.userList.length);
    },
    /** 选择用户开始聊天 */
    handleSelectUser(user) {
      this.currentUserId = user.id;
      this.currentUserName = user.nickname;
      this.currentOrderId = user.orderId;
      this.currentOrderNo = user.orderNo;
      user.unread = false;

      // 请求历史记录
      request({
        url: '/visa/order/chat/history',
        method: 'get',
        params: {
          orderId: user.orderId,
          customerId: user.id // ★ 核心隔离逻辑
        }
      }).then(res => {
        this.messageList = res.data;
        this.scrollToBottom();
      });
    },
    /** 发送消息 */
    handleSend() {
      if (!this.inputText.trim()) return;

      // ★★★ 核心修复：必须包含 orderId ★★★
      const msgObj = {
        toUserId: this.currentUserId.toString(), // 发给谁
        fromUserId: 'admin',                     // 我是谁
        content: this.inputText,                 // 聊啥
        // 这里要从当前选中的用户对象里拿 orderId，如果没有就传 0
        orderId: this.currentUserOrderId || 0,
        senderType: 2                            // 2代表管理员
      };

      // 1. 通过 WebSocket 发送给后端
      this.socket.send(JSON.stringify(msgObj));

      // 2. 存入当前显示的列表（即时显示）
      this.messageList.push({
        senderType: 2,
        content: this.inputText,
        createTime: new Date()
      });

      this.inputText = '';
      this.scrollToBottom();
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const box = this.$refs.messageBox;
        if (box) box.scrollTop = box.scrollHeight;
      });
    },
    loadChatUsers() {
      request({ url: '/visa/order/chat/users', method: 'get' }).then(res => {
        // 若依的 TableDataInfo 格式，数据在 rows 里
        this.userList = (res.rows || []).map(item => ({
          id: item.customerId,    // 用于对话的 ID
          orderId: item.id,       // 关联的订单ID (SQL里我们做了别名)
          orderNo: item.orderNo,
          nickname: item.customerName || (item.customerId == 0 ? '游客' : '客户' + item.customerId),
          lastMsg: item.lastMsg,
          unread: false
        }));
      });
    },
    /** 点击查看订单详情 */
    handleViewOrder() {
      if (!this.currentOrderId) return;

      // 1. 开启 Loading
      const loading = this.$loading({ lock: true, text: '加载订单资料...' });

      // 2. 调用你之前在管理端已经有的获取订单详情 API
      // 注意：确保接口路径正确，通常是 /visa/order/{id}
      request({
        url: '/visa/order/' + this.currentOrderId,
        method: 'get'
      }).then(res => {
        this.orderDetail = res.data;
        this.orderDetailOpen = true;
      }).finally(() => {
        loading.close();
      });
    },

    // 别忘了把之前的辅助方法也粘过来（getUrl, isImage, parseMaterials, getPersonSuppFiles）
    getPersonSuppFiles(order, index) {
      if (!order.supplementaryMaterials) return [];
      try {
        const data = JSON.parse(order.supplementaryMaterials);
        return data[index.toString()] || [];
      } catch (e) { return []; }
    },
    // 1. 解析材料 JSON 字符串
    parseMaterials(jsonStr) {
      if (!jsonStr) return [];
      try {
        // 假设后端存的是 JSON 字符串
        return JSON.parse(jsonStr);
      } catch (e) {
        console.error("解析材料失败", e);
        return [];
      }
    },

    // 2. 判断是否是图片 (用于显示 el-image 还是文字)
    isImage(path) {
      if (typeof path !== 'string') return false;
      const reg = /\.(jpg|jpeg|png|gif|webp|bmp)$/i;
      return reg.test(path);
    },

    // 3. 拼接图片完整地址 (根据你后端的资源映射修改)
    getUrl(path) {
      if (!path) return '';
      if (path.startsWith('http')) return path;
      // 假设你的后端静态资源前缀是 process.env.VUE_APP_BASE_API
      return process.env.VUE_APP_BASE_API + path;
    },

    // 4. 根据状态返回 Tag 的颜色类型
    getStatusType(status) {
      const map = {
        0: 'info',
        1: 'primary',
        2: 'warning',
        3: '',
        4: 'success',
        5: 'danger'
      };
      return map[status] || 'info';
    },

    // 5. 文件预览 (补件详情)
    previewFile(url) {
      window.open(this.getUrl(url), '_blank');
    },

    // 6. 之前代码里的 getPersonSuppFiles 保持不变
    getPersonSuppFiles(order, index) {
      if (!order || !order.supplementaryMaterials) return [];
      try {
        const data = JSON.parse(order.supplementaryMaterials);
        return data[index.toString()] || [];
      } catch (e) { return []; }
    },
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