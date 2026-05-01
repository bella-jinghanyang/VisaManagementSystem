<template>
    <div class="specialist-chat-page">
        <div class="chat-container container-width">
            <!-- 左侧：订单切换 -->
            <div class="order-sidebar">
                <div class="sidebar-header">关联订单</div>
                <div class="order-list-scroll">
                    <div v-for="order in myOrders" :key="order.id"
                        :class="['order-nav-item', { active: currentOrderId === order.id }]"
                        @click="handleSelectOrder(order)">
                        <div class="nav-img-box">
                            <img :src="$getImgUrl(parseSnapshot(order.productSnapshot).image)" class="prod-thumb">
                        </div>
                        <div class="nav-info">
                            <div class="nav-title">{{ parseSnapshot(order.productSnapshot).title }}</div>
                            <div class="nav-no">单号: {{ order.orderNo }}</div>
                        </div>
                        <div class="active-indicator"></div>
                    </div>
                    <div v-if="myOrders.length === 0" class="empty-orders">暂无进行中的订单</div>
                </div>
            </div>

            <!-- 右侧：主聊天窗口 -->
            <div class="chat-main">
                <template v-if="currentOrderId">
                    <div class="chat-header">
                        <div class="advisor-info">
                            <div class="status-dot"></div>
                            <span>专属签证顾问 - 正在为您服务</span>
                        </div>
                        <div class="order-summary-tag">当前订单：{{ selectedOrderNo }}</div>
                    </div>

                    <!-- 消息列表 -->
                    <div class="message-list" ref="messageBox">
                        <div v-for="(msg, idx) in messageList" :key="idx"
                            :class="['msg-row', msg.senderType === 2 ? 'msg-admin' : 'msg-user']">
                            <div class="avatar-box">
                                <el-avatar :size="36"
                                    :icon="msg.senderType === 2 ? 'el-icon-headset' : 'el-icon-user'"></el-avatar>
                            </div>
                            <div class="msg-bubble">
                                <div class="msg-content">{{ msg.content }}</div>
                                <div class="msg-time">{{ formatChatTime(msg.createTime) }}</div>
                            </div>
                        </div>
                        <div v-if="messageList.length === 0" class="first-greet">
                            👋 您好！我是您的专属顾问，关于此订单的任何问题请直接留言。
                        </div>
                    </div>

                    <!-- 输入区域 -->
                    <div class="input-panel">
                        <div class="input-wrapper">
                            <textarea v-model="inputText" placeholder="在此输入您的咨询内容..."
                                @keyup.enter.ctrl="sendToSpecialist"></textarea>
                            <div class="action-bar">
                                <span class="tip">Ctrl + Enter 换行</span>
                                <el-button type="primary" round class="send-btn" @click="sendToSpecialist"
                                    :disabled="!inputText.trim()">
                                    发送 <i class="el-icon-s-promotion"></i>
                                </el-button>
                            </div>
                        </div>
                    </div>
                </template>

                <div v-else class="empty-chat-holder">
                    <div class="holder-inner">
                        <i class="el-icon-chat-dot-round animate-bounce"></i>
                        <h3>专属顾问服务</h3>
                        <p>请在左侧选择需要咨询的订单，签证顾问将为您提供一对一专业指导</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
/* eslint-disable */
import request from '@/utils/request';


export default {
    data() {
        return {
            myOrders: [],
            currentOrderId: null,
            selectedOrderNo: '',
            messageList: [],
            inputText: '',
            socket: null,
            // 这里的 Token Key 必须和 Navbar 保持一致
            token: localStorage.getItem('Client-Token')
        }
    },
    created() {
        this.loadMyOrders();
        this.initWebSocket();
    },
    methods: {
        initWebSocket() {
            const userStr = localStorage.getItem('Client-User');
            if (!userStr) return;
            const user = JSON.parse(userStr);

            // 关闭旧连接，防止重连时产生连接泄漏
            if (this.socket && this.socket.readyState !== WebSocket.CLOSED) {
                this.socket.close();
            }

            // 优先使用环境变量；若未配置则直连后端 8080（与 AI 咨询页行为一致）
            const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsBase = process.env.VUE_APP_WS_URL || `${wsProtocol}//localhost:8080`;
            this.socket = new WebSocket(`${wsBase}/websocket/chat/${user.id}`);

            this.socket.onopen = () => {
                console.log('>>> 人工咨询 WebSocket 连接成功');
            };

            this.socket.onmessage = (e) => {
                const data = JSON.parse(e.data);
                // ★ 核心逻辑：只有属于当前选中订单的消息才压入列表
                if (data.orderId === this.currentOrderId && data.isAi === '0') {
                    this.messageList.push({
                        senderType: 2,
                        content: data.content,
                        createTime: new Date()
                    });
                    this.$nextTick(() => this.scrollToBottom());
                }
            };

            this.socket.onclose = () => {
                console.warn('<<< 人工咨询 WebSocket 连接已断开');
            };

            this.socket.onerror = () => {
                this.$message && this.$message.error("聊天连接失败，请检查后端是否启动");
            };
        },
        loadMyOrders() {
            const user = JSON.parse(localStorage.getItem('Client-User'));
            // 调用你之前写的 C 端获取列表接口
            request({ url: '/client/order/list/' + user.id, method: 'get' }).then(res => {
                this.myOrders = res.data || [];
                // 自动选中第一个单子（如果 URL 没传参数的话）
                if (this.myOrders.length > 0 && !this.$route.query.orderId) {
                    this.handleSelectOrder(this.myOrders[0]);
                }
            });
        },
        handleSelectOrder(order) {
            this.currentOrderId = order.id;
            this.selectedOrderNo = order.orderNo;

            // ★ 核心修复：调用接口时明确加上身份标记，确保后端不按 AI 逻辑处理
            request({
                url: '/visa/order/chat/history',
                method: 'get',
                params: {
                    orderId: order.id,
                    customerId: order.customerId,
                    isAi: '0' // 只看人工
                }
            }).then(res => {
                this.messageList = res.data || [];
                this.$nextTick(() => this.scrollToBottom());
            });
        },
        sendToSpecialist() {
            if (!this.inputText.trim()) return;
            const user = JSON.parse(localStorage.getItem('Client-User'));

            // 检查连接状态，防止在 socket 未就绪时调用 send() 抛异常
            if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
                this.$message && this.$message.warning("聊天连接已断开，正在重连...");
                this.initWebSocket();
                return;
            }

            const msg = {
                toUserId: 'admin',
                fromUserId: user.id.toString(),
                content: this.inputText,
                orderId: this.currentOrderId,
                isAi: '0'
            };

            this.socket.send(JSON.stringify(msg));

            // 本地存入
            this.messageList.push({
                senderType: 1,
                content: this.inputText,
                createTime: new Date()
            });

            this.inputText = '';
            this.$nextTick(() => this.scrollToBottom());
        },
        scrollToBottom() {
            const box = this.$refs.messageBox;
            if (box) box.scrollTop = box.scrollHeight;
        },
        parseSnapshot(json) { try { return JSON.parse(json || '{}') } catch (e) { return {} } },
        formatChatTime(time) {
            if (!time) return '';
            const date = new Date(time);

            // 如果是今天的消息，只显示：时:分
            // 如果不是今天，可以显示：月-日 时:分
            const h = date.getHours().toString().padStart(2, '0');
            const i = date.getMinutes().toString().padStart(2, '0');

            // 简单的格式化逻辑
            return `${h}:${i}`;
        },
    }
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/variables.scss";

.specialist-chat-page {
    background: #F5F5F7;
    min-height: 100vh;
    /* ★ 修复：向下偏移，躲开 Navbar ★ */
    padding-top: 100px;
    padding-bottom: 50px;
}

.chat-container {
    display: flex;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(20px);
    border-radius: 28px;
    height: 700px;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.4);
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
}

/* 侧边栏：订单列表 */
.order-sidebar {
    width: 320px;
    background: rgba(255, 255, 255, 0.5);
    border-right: 1px solid rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
}

.sidebar-header {
    padding: 25px;
    font-size: 18px;
    font-weight: 700;
    color: #1d1d1f;
}

.order-list-scroll {
    flex: 1;
    overflow-y: auto;
    padding: 0 15px;
}

.order-nav-item {
    display: flex;
    align-items: center;
    padding: 15px;
    border-radius: 18px;
    cursor: pointer;
    margin-bottom: 10px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;

    &:hover {
        background: rgba(0, 122, 255, 0.05);
    }

    &.active {
        background: #fff;
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);

        .nav-title {
            color: $primary-color;
        }

        .active-indicator {
            height: 24px;
            opacity: 1;
        }
    }
}

.prod-thumb {
    width: 48px;
    height: 36px;
    border-radius: 6px;
    object-fit: cover;
}

.nav-info {
    margin-left: 15px;
    flex: 1;
    overflow: hidden;
}

.nav-title {
    font-size: 14px;
    font-weight: 600;
    color: #1d1d1f;
    margin-bottom: 3px;
}

.nav-no {
    font-size: 11px;
    color: #86868b;
}

.active-indicator {
    width: 4px;
    height: 0;
    background: $primary-color;
    border-radius: 2px;
    position: absolute;
    left: 0;
    opacity: 0;
    transition: all 0.3s;
}

/* 右侧聊天主区 */
.chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    position: relative;
}

.chat-header {
    padding: 20px 30px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    display: flex;
    justify-content: space-between;
    align-items: center;

    .advisor-info {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 700;
        font-size: 16px;

        .status-dot {
            width: 8px;
            height: 8px;
            background: #34C759;
            border-radius: 50%;
            box-shadow: 0 0 8px #34C759;
        }
    }

    .order-summary-tag {
        font-size: 12px;
        color: #86868b;
        background: #f0f0f2;
        padding: 4px 12px;
        border-radius: 20px;
    }
}

/* 消息列表气泡 */
.message-list {
    flex: 1;
    overflow-y: auto;
    padding: 30px;
    background: url('https://www.transparenttextures.com/patterns/cubes.png'); // 增加极淡的背景纹理
}

.msg-row {
    display: flex;
    margin-bottom: 25px;
    gap: 12px;

    &.msg-user {
        flex-direction: row-reverse;

        .msg-bubble {
            background: $primary-color;
            color: #fff;
            border-radius: 20px 20px 4px 20px;
        }

        .msg-time {
            text-align: right;
            color: rgba(255, 255, 255, 0.7);
        }
    }

    &.msg-admin {
        .msg-bubble {
            background: #fff;
            color: #1d1d1f;
            border-radius: 20px 20px 20px 4px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
        }

        .msg-time {
            color: #86868b;
        }
    }
}

.msg-bubble {
    max-width: 65%;
    padding: 12px 18px;

    .msg-content {
        font-size: 15px;
        line-height: 1.5;
        word-break: break-all;
    }

    .msg-time {
        font-size: 10px;
        margin-top: 5px;
    }
}

/* 输入面板 */
.input-panel {
    padding: 20px 30px 30px;
    background: rgba(255, 255, 255, 0.4);
}

.input-wrapper {
    background: #fff;
    border-radius: 20px;
    padding: 15px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03);

    textarea {
        width: 100%;
        border: none;
        outline: none;
        resize: none;
        font-size: 15px;
        color: #1d1d1f;
        height: 60px;

        &::placeholder {
            color: #c7c7cc;
        }
    }
}

.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;

    .tip {
        font-size: 12px;
        color: #c7c7cc;
    }

    .send-btn {
        padding: 8px 24px;
        font-weight: 600;
        border-radius: 12px !important;
    }
}

/* 空状态 */
.empty-chat-holder {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;

    .holder-inner i {
        font-size: 64px;
        color: #d2d2d7;
        margin-bottom: 20px;
        display: block;
    }

    h3 {
        font-size: 24px;
        color: #1d1d1f;
        margin-bottom: 10px;
    }

    p {
        color: #86868b;
        max-width: 300px;
        line-height: 1.5;
    }
}

.animate-bounce {
    animation: bounce 2s infinite;
}

@keyframes bounce {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-10px);
    }
}
</style>