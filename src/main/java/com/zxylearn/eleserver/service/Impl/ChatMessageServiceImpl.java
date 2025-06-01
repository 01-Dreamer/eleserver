package com.zxylearn.eleserver.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxylearn.eleserver.mapper.ChatMessageMapper;
import com.zxylearn.eleserver.pojo.entity.ChatMessage;
import com.zxylearn.eleserver.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageServiceImpl
        extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {


    @Override
    public boolean addChatMessage(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return false;
        }
        return baseMapper.insert(chatMessage) > 0;
    }

    @Override
    public List<ChatMessage> getChatMessageByUserId(Integer userId1, Integer userId2) {
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                        .eq("senderId", userId1)
                        .eq("receiverId", userId2))
                .or(wrapper -> wrapper
                        .eq("senderId", userId2)
                        .eq("receiverId", userId1))
                .orderByAsc("createTime");
        return baseMapper.selectList(queryWrapper);
    }


}
