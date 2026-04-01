package com.ruoyi.web.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.visa.domain.OrderMessage;
import com.ruoyi.visa.service.IOrderMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket/chat/{userId}")
@Component
public class ChatWebSocketServer {

    // 1. 静态 Map 存储在线会话
    private static Map<String, Session> sessionPool = new ConcurrentHashMap<>();

    // ★ 关键：因为 WebSocket 不是单例，通过静态变量注入 Service ★
    private static IOrderMessageService messageService;

    @Autowired
    public void setMessageService(IOrderMessageService messageService) {
        ChatWebSocketServer.messageService = messageService;
    }

    /** 连接建立成功 */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessionPool.put(userId, session);
        System.out.println(">>> WebSocket连接成功: 用户ID=" + userId + "，当前在线人数: " + sessionPool.size());
    }

    /** 收到客户端消息 */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        System.out.println(">>> 收到原始消息: " + message);
        if (StringUtils.isEmpty(message)) return;

        try {
            JSONObject json = JSON.parseObject(message);
            String toUserId = json.getString("toUserId");
            String content = json.getString("content");
            Long orderId = json.getLong("orderId");
            if (orderId == null) orderId = 0L;

            // ★★★ 核心逻辑：分流判定 ★★★
            String isAiFlag = "0"; // 默认判定为人工

            if (!"admin".equals(userId)) {
                // 如果发送者是客户
                if (orderId > 0) {
                    // 情况 1：带订单号咨询。强制设为“人工”，不触发 AI
                    isAiFlag = "0";
                } else {
                    // 情况 2：不带订单号咨询。判定为“智能助手”消息
                    isAiFlag = "1";
                }
            } else {
                // 情况 3：管理员回复。永远是“人工”
                isAiFlag = "0";
            }

            // 1. 注入发送者ID
            json.put("fromUserId", userId);
            json.put("isAi", isAiFlag);

            // 2. 转发逻辑 (实时推送)
            Session toSession = sessionPool.get(toUserId);
            if (toSession != null && toSession.isOpen()) {
                toSession.getAsyncRemote().sendText(json.toJSONString());
            }

            // 3. 构造数据库存储对象
            OrderMessage orderMsg = new OrderMessage();
            orderMsg.setContent(content);
            orderMsg.setOrderId(orderId);
            orderMsg.setIsAi(isAiFlag); // ★ 标记这一条是 AI 还是 人工
            orderMsg.setCreateTime(com.ruoyi.common.utils.DateUtils.getNowDate());

            // 处理客户归属逻辑 (保持你之前的逻辑)
            if ("admin".equals(userId)) {
                orderMsg.setSenderType(2);
                orderMsg.setCustomerId("guest".equals(toUserId) ? 0L : Long.valueOf(toUserId));
            } else {
                orderMsg.setSenderType(1);
                orderMsg.setCustomerId("guest".equals(userId) ? 0L : Long.valueOf(userId));
            }

            // 4. 保存原始消息到数据库
            messageService.insertOrderMessage(orderMsg);

            // ★★★ 5. AI 自动回复逻辑 ★★★
            if ("1".equals(isAiFlag)) {
                // 只有当 orderId 为 0 (非专属顾问模式) 且标记为 AI 时，才调用 AI 服务
                System.out.println(">>> 触发智能助手回复逻辑...");

                // 这里调用你之前的 AI 回复方法 (假设方法名为 handleAiReply)
                // 你需要确保该方法会产生一个新的消息包发回给客户
                // handleAiReply(userId, content);
            } else {
                System.out.println(">>> 专属顾问模式/管理员回复，不触发 AI");
            }

        } catch (Exception e) {
            System.err.println("WebSocket消息处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /** 连接关闭 */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        sessionPool.remove(userId);
        System.out.println("<<< WebSocket连接断开: 用户ID=" + userId);
    }

    /** 发生错误 */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}