package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.pojo.ChatMessage;
import com.zxylearn.eleserver.pojo.EleBusiness;
import com.zxylearn.eleserver.service.ChatMessageService;
import com.zxylearn.eleserver.service.ElasticsearchService;
import com.zxylearn.eleserver.service.OssService;
import com.zxylearn.eleserver.service.UserService;
import com.zxylearn.eleserver.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    ///////////////////////////////////////////////////
    /// 获取聊天信息
    ///////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////
    /// 获取用户头像
    ///////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////
    /// 更新用户头像
    ///////////////////////////////////////////////////
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

        String newAvatar = ossService.uploadFile(avatar);
        String oldAvatar = userService.getAvatarByUserId(userId);
        if (!userService.updateAvatar(userId, newAvatar)) {
            log.warn("failed to update avatar, userId={} avatar={}", userId, newAvatar);
        }
        if (oldAvatar != null && !ossService.deleteFile(oldAvatar)) {
            log.warn("failed to delete old avatar: {}", oldAvatar);
        }

        return newAvatar;
    }

    ///////////////////////////////////////////////////
    /// 更新商店封面
    ///////////////////////////////////////////////////
    @PostMapping("updateStoreCover")
    public String updateStoreCover(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile storeCover
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

        String userId = JwtUtil.getUserId(accessToken).toString();

        EleBusiness eleBusiness = elasticsearchService.getEleBusinessById(userId);
        if (eleBusiness == null) {
            return null;
        }

        String newStoreCover = ossService.uploadFile(storeCover);
        String oldStoreCover = eleBusiness.getStoreCover();
        if (!elasticsearchService.updateStoreCover(userId, newStoreCover)) {
            log.warn("failed to update store cover, userId={} avatar={}", userId, newStoreCover);
        }
        if (oldStoreCover != null && !ossService.deleteFile(oldStoreCover)) {
            log.warn("failed to delete old store cover: {}", oldStoreCover);
        }

        return newStoreCover;
    }

    ///////////////////////////////////////////////////
    /// 更新商店信息
    ///////////////////////////////////////////////////
    @PostMapping("updateStoreText")
    public boolean updateStoreText(
            HttpServletRequest request,
            @RequestParam String storeName,
            @RequestParam String storeDescription
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return false;
        }

        String userId = JwtUtil.getUserId(accessToken).toString();
        return elasticsearchService.updateStoreText(userId, storeName, storeDescription);
    }

    ///////////////////////////////////////////////////
    /// 增加商店销量
    ///////////////////////////////////////////////////
    @PostMapping("addStoreVolume")
    public boolean addStoreVolume(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return false;
        }

        String userId = JwtUtil.getUserId(accessToken).toString();
        return elasticsearchService.addStoreVolume(userId);
    }

    ///////////////////////////////////////////////////
    /// 增加商品
    ///////////////////////////////////////////////////
    @PostMapping("addStoreItem")
    public Integer addStoreItem(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return -1;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return -1;
        }

        String userId = JwtUtil.getUserId(accessToken).toString();
        return elasticsearchService.addStoreItem(userId);
    }

    ///////////////////////////////////////////////////
    /// 删除商品
    ///////////////////////////////////////////////////
    @DeleteMapping("delStoreItem")
    public boolean delStoreItem(
            HttpServletRequest request,
            @RequestParam Integer itemId
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return false;
        }

        String userId = JwtUtil.getUserId(accessToken).toString();

        String oldItemCover = elasticsearchService.getStoreItemCover(userId, itemId);
        if (oldItemCover != null && !ossService.deleteFile(oldItemCover)) {
            log.warn("failed to delete old item cover: {}", oldItemCover);
        }
        return elasticsearchService.delStoreItem(userId,itemId);
    }

    ///////////////////////////////////////////////////
    /// 更新商品封面
    ///////////////////////////////////////////////////
    @PostMapping("updateStoreItemCover")
    public String updateStoreItemCover(
            HttpServletRequest request,
            @RequestParam Integer itemId,
            @RequestParam("file") MultipartFile itemCover
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

        String userId = JwtUtil.getUserId(accessToken).toString();

        String newItemCover = ossService.uploadFile(itemCover);
        String oldItemCover = elasticsearchService.getStoreItemCover(userId, itemId);
        if (!elasticsearchService.updateStoreItemCover(userId, itemId, newItemCover)) {
            log.warn("failed to update item cover, userId={} avatar={}", userId, newItemCover);
        }
        if (oldItemCover != null && !ossService.deleteFile(oldItemCover)) {
            log.warn("failed to delete old item cover: {}", oldItemCover);
        }

        return newItemCover;
    }

    ///////////////////////////////////////////////////
    /// 增加商品信息
    ///////////////////////////////////////////////////
    @PostMapping("updateStoreItemText")
    public boolean updateStoreItemText(
            HttpServletRequest request,
            @RequestParam Integer itemId,
            @RequestParam String itemName,
            @RequestParam String itemDescription,
            @RequestParam Float itemPrice
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return false;
        }

        String userId = JwtUtil.getUserId(accessToken).toString();
        return elasticsearchService.updateStoreItemText(userId, itemId, itemName, itemDescription, itemPrice);
    }

    ///////////////////////////////////////////////////
    /// 获取所有商家信息
    ///////////////////////////////////////////////////
    @GetMapping("getAllEleBusiness")
    public List<EleBusiness> getAllEleBusiness(
            HttpServletRequest request
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

        String userId = JwtUtil.getUserId(accessToken).toString();
        return elasticsearchService.getAllEleBusinessOrderById(userId);
    }


}
