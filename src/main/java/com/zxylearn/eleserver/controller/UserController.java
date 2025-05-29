package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.pojo.ChatMessage;
import com.zxylearn.eleserver.service.ChatMessageService;
import com.zxylearn.eleserver.service.ElasticsearchService;
import com.zxylearn.eleserver.service.OssService;
import com.zxylearn.eleserver.service.UserService;
import com.zxylearn.eleserver.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@Slf4j
@RestController
public class UserController {
    @Autowired
    private OssService ossService;

    @Autowired
    private UserService userService;

    @Autowired
    ElasticsearchService elasticsearchService;

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
        if (userId.equals(id1) || userId.equals(id2)) {
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


    @PostMapping("/updateAvatar")
    public String updateAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile avatar
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

        Integer userId = JwtUtil.getUserId(accessToken);

        String new_avatar = ossService.uploadFile(avatar);
        String old_avatar = userService.getAvatarByUserId(userId);
        if (!userService.updateAvatar(userId, new_avatar)) {
            log.warn("failed to update avatar, userId={} avatar={}", userId, new_avatar);
        }
        if (old_avatar != null && !ossService.deleteFile(old_avatar)) {
            log.warn("failed to delete old avatar: {}", old_avatar);
        }


        return new_avatar;
    }

    @PostMapping("updateStoreCover")
    public String updateStoreCover(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile storeCover
    ) {

        /*String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return null;
        }*/


        return ossService.uploadFile(storeCover);
    }


    @PostMapping("updateItemCover")
    public String updateItemCover(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile itemCover
    ) {

        /*String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return null;
        }*/


        return ossService.uploadFile(itemCover);
    }
}
