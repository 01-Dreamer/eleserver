package com.zxylearn.eleserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxylearn.eleserver.pojo.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
