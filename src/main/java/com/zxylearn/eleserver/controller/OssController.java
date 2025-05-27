package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.service.OssService;
import com.zxylearn.eleserver.service.UserService;
import com.zxylearn.eleserver.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class OssController {

    @Autowired
    private OssService ossService;

    @Autowired
    private UserService userService;


    @PostMapping("/uploadAvatar")
    public String uploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file
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


        String new_avatar = ossService.uploadFile(file);
        String old_avatar = userService.getAvatarByUserId(userId);
        if (!userService.updateAvatar(userId, new_avatar)) {
            log.error("failed to update avatar, userId={} avatar={}", userId, new_avatar);
        }
        if (old_avatar != null && !ossService.deleteFile(old_avatar)) {
            log.error("failed to delete old avatar: {}", old_avatar);
        }


        return new_avatar;
    }
}
