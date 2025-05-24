package com.zxylearn.eleserver.handler;

import com.zxylearn.eleserver.pojo.ChatMessage;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArraySet;

public class WebSocketHandler extends TextWebSocketHandler {

    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("连接建立：" + session.getId());
        session.sendMessage(new TextMessage("欢迎连接 WebSocket，您的 sessionId 是：" + session.getId()));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println(parseMessage(payload).toString());
        System.out.println("收到消息：" + payload + " 来自 sessionId：" + session.getId());
        session.sendMessage(new TextMessage("你发送了: " + payload));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        System.out.println("连接关闭，sessionId: " + session.getId() + "，状态：" + status);
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("发生错误，sessionId: " + session.getId() + "，错误: " + exception.getMessage());
        exception.printStackTrace();
        if (session.isOpen()) {
            session.close();
        }
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private static ChatMessage parseMessage(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            ChatMessage message = new ChatMessage();

            message.setSenderId(json.getInt("senderId"));
            message.setReceiverId(json.getInt("receiverId"));
            message.setContent(json.getString("content"));

            return message;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
