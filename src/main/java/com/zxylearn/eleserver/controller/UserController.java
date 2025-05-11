package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.mapper.UserMapper;
import com.zxylearn.eleserver.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/all")
    public List<User> all() {
        return userMapper.selectList(null);
    }

}
