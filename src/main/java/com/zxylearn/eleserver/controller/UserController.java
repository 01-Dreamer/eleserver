package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.pojo.User;
import com.zxylearn.eleserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @GetMapping("/all")
    public List<User> all() {
        System.out.println(accessKeySecret);
        return userService.list();
    }


}
