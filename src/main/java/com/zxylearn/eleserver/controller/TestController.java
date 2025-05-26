package com.zxylearn.eleserver.controller;


import com.zxylearn.eleserver.pojo.ChatMessage;
import com.zxylearn.eleserver.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class TestController {

    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping("/test")
    public List<ChatMessage> test() {
        return chatMessageService.getChatMessageByUserId(1, 2);
    }


}