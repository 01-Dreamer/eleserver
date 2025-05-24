package com.zxylearn.eleserver.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ChatMessage")
public class ChatMessage {
    @TableId(value = "messageId", type = IdType.AUTO)
    private Integer messageId;

    @TableField(value = "senderId")
    private Integer senderId;

    @TableField(value = "receiverId")
    private Integer receiverId;

    @TableField(value = "content")
    private String content;

    @TableField(value = "createTime")
    private LocalDateTime createTime;
}
