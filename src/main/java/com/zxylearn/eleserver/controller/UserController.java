package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.pojo.ChatMessage;
import com.zxylearn.eleserver.pojo.User;
import com.zxylearn.eleserver.service.ChatMessageService;
import com.zxylearn.eleserver.service.OssService;
import com.zxylearn.eleserver.service.UserService;
import com.zxylearn.eleserver.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping("/getChatMessage")
    public List<ChatMessage> getChatMessage(
            HttpServletRequest request,
            @RequestParam Integer id1,
            @RequestParam Integer id2
    ) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return null;
        }

        // 只允许用户请求和自己相关的聊天记录
        Integer userId = JwtUtil.getUserId(accessToken);
        if(userId.equals(id1) || userId.equals(id2)) {
            return chatMessageService.getChatMessageByUserId(id1, id2);
        } else {
            return null;
        }
    }


    @GetMapping("/getOtherAvatar")
    public String getOtherAvatar(
            HttpServletRequest request,
            @RequestParam Integer id
    ) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return null;
        }

        String avatar = userService.getAvatarByUserId(id);
        if (avatar == null) {
            return "https://zxydata.oss-cn-chengdu.aliyuncs.com/ele/DefaultAvatar.png";
        } else {
            return avatar;
        }
    }
}
