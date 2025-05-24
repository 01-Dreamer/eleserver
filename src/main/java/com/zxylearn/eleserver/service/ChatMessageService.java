package com.zxylearn.eleserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxylearn.eleserver.pojo.ChatMessage;

public interface ChatMessageService extends IService<ChatMessage> {
    boolean addChatMessage(ChatMessage chatMessage);
}
