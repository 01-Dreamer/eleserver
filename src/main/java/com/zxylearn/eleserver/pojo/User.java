package com.zxylearn.eleserver.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("User")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "email")
    private String email;

    @TableField(value = "password")
    private String password;
}
