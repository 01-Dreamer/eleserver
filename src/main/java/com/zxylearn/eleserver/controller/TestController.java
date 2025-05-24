package com.zxylearn.eleserver.controller;


import com.zxylearn.eleserver.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping("/test")
    public String test() {
        return chatMessageService.getById(6).toString();
    }


}