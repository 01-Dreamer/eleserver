package com.zxylearn.eleserver.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Integer messageId;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private LocalDateTime createTime;
}
