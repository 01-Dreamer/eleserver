package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.pojo.LoginRequest;
import com.zxylearn.eleserver.pojo.User;
import com.zxylearn.eleserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public List<User> all() {
        return userService.list();
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        System.out.println("Received login request:");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Password: " + request.getPassword());

        return "login";
    }

}
