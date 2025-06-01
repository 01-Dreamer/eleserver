package com.zxylearn.eleserver.pojo.entity;

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
@TableName("PayOrder")
public class PayOrder {
    @TableId(value = "orderId", type = IdType.AUTO)
    private String orderId;

    @TableField(value = "userId")
    private String userId;

    @TableField(value = "orderInfo")
    private String orderInfo;

    @TableField(value = "createTime")
    private LocalDateTime createTime;

    @TableField(value = "isPay")
    private boolean isPay;
}
