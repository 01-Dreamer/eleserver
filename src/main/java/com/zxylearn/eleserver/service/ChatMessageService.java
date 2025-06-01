package com.zxylearn.eleserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxylearn.eleserver.pojo.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {
    boolean addChatMessage(ChatMessage chatMessage);
    List<ChatMessage> getChatMessageByUserId(Integer id1, Integer id2);
}
