package com.zxylearn.eleserver.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxylearn.eleserver.mapper.ChatMessageMapper;
import com.zxylearn.eleserver.pojo.ChatMessage;
import com.zxylearn.eleserver.service.ChatMessageService;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageServiceImpl
        extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {


    @Override
    public boolean addChatMessage(ChatMessage chatMessage) {
        if(chatMessage == null){
            return false;
        }
        return baseMapper.insert(chatMessage) > 0;
    }
}
