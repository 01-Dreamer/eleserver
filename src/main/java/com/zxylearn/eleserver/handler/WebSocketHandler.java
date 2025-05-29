package com.zxylearn.eleserver.handler;


import com.zxylearn.eleserver.pojo.ChatMessage;
import com.zxylearn.eleserver.pojo.User;
import com.zxylearn.eleserver.service.ChatMessageService;
import com.zxylearn.eleserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final Map<Integer, WebSocketSession> sessions;

    public WebSocketHandler(UserService userService,
                            ChatMessageService chatMessageService
    ) {
        this.userService = userService;
        this.chatMessageService = chatMessageService;
        this.sessions = new ConcurrentHashMap<>();
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        Integer userId = (Integer) session.getAttributes().get("userId");
        if (sessions.containsKey(userId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            log.warn("User id = {} tried to connect again, connection rejected", userId);
            return;
        }
        sessions.put(userId, session);
        log.info("User id = {} connected", userId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ChatMessage chatMessage = parseMessage(payload);
        if (chatMessage == null) {
            return;
        }

        Integer receiverId = chatMessage.getReceiverId();
        if (!userService.existsById(receiverId)) {
            log.warn("failed to find receiver, receiverId={}", receiverId);
            return;
        }

        chatMessage.setCreateTime(LocalDateTime.now());
        if (!chatMessageService.addChatMessage(chatMessage)) {
            log.error("failed to add chat message: {}", chatMessage);
        }

        String messageJson = convertChatMessageToJson(chatMessage);
        if (messageJson == null) {
            return;
        }
        session.sendMessage(new TextMessage(messageJson));

        Integer toUserId = chatMessage.getReceiverId();
        WebSocketSession toSession = sessions.get(toUserId);
        if (toSession == null) {
            return;
        }

        toSession.sendMessage(new TextMessage(messageJson));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        sessions.remove(userId);
        log.info("User id = {} disconnected, status code = {}, reason = {}",
                userId, status.getCode(), status.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Integer userId = (Integer) session.getAttributes().get("userId");
        sessions.remove(userId);
        if (session.isOpen()) {
            session.close();
        }
        log.error("websocket error for user id = {}: {}", userId, exception.getMessage());
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
            log.error("parse payload {} json error: {}", payload, e.getMessage());
            return null;
        }
    }

    public static String convertChatMessageToJson(ChatMessage message) {
        try {
            JSONObject json = new JSONObject();

            json.put("senderId", message.getSenderId());
            json.put("receiverId", message.getReceiverId());
            json.put("content", message.getContent());

            LocalDateTime createTime = message.getCreateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedTime = createTime.format(formatter);
            json.put("createTime", formattedTime);

            return json.toString();
        } catch (JSONException e) {
            log.error("failed to convert ChatMessage to JSON: {}", e.getMessage());
            return null;
        }
    }
}
