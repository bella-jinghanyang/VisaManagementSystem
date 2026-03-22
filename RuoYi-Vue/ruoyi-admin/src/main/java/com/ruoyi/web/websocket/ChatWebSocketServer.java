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
            String toUserId = json.getString("toUserId"); // 接收者
            String content = json.getString("content");   // 内容
            Long orderId = json.getLong("orderId");       // 关联的订单ID

            // 1. 注入发送者ID，确保B端前端收到消息后知道是谁发的
            json.put("fromUserId", userId);

            // 2. 转发逻辑
            Session toSession = sessionPool.get(toUserId);
            if (toSession != null && toSession.isOpen()) {
                toSession.getAsyncRemote().sendText(json.toJSONString());
            }

            // 3. ★★★ 核心存储逻辑：解决身份隔离与记录保存 ★★★
            OrderMessage orderMsg = new OrderMessage();
            orderMsg.setContent(content);
            orderMsg.setOrderId(orderId != null ? orderId : 0L);
            orderMsg.setCreateTime(com.ruoyi.common.utils.DateUtils.getNowDate());

            // --- 关键判断：这笔消息属于哪个客户的“聊天档案” ---
            if ("admin".equals(userId)) {
                // 情况A：发送者是管理员 -> 说明是管理员在回复
                // 此时消息应该归属于【接收者】对应的客户档案
                orderMsg.setSenderType(2); // 2代表管理员
                try {
                    // 如果接收者是guest，存0；否则存对应的数字ID
                    orderMsg.setCustomerId("guest".equals(toUserId) ? 0L : Long.valueOf(toUserId));
                } catch (Exception e) {
                    orderMsg.setCustomerId(0L); // 兜底
                }
            } else {
                // 情况B：发送者不是管理员 -> 说明是客户在咨询
                // 此时消息归属于【发送者】自己
                orderMsg.setSenderType(1); // 1代表客户
                try {
                    orderMsg.setCustomerId("guest".equals(userId) ? 0L : Long.valueOf(userId));
                } catch (Exception e) {
                    orderMsg.setCustomerId(0L);
                }
            }

            // 4. 保存到数据库
            // 只要有了 customerId 和 orderId，两端查询历史记录时加上这两个条件过滤，
            // 就能实现不同人的聊天记录完全隔离！
            messageService.insertOrderMessage(orderMsg);

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