package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.pojo.entity.ChatMessage;
import com.zxylearn.eleserver.pojo.entity.EleBusiness;
import com.zxylearn.eleserver.pojo.entity.PayOrder;
import com.zxylearn.eleserver.service.*;
import com.zxylearn.eleserver.utils.JsonUtil;
import com.zxylearn.eleserver.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private JsonUtil jsonUtil;

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
    /// 更新商品信息
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
    /// 获取所有商家信息（不包括StoreItems）
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

    ///////////////////////////////////////////////////
    /// 获取指定商家信息
    ///////////////////////////////////////////////////
    @GetMapping("getOneEleBusiness")
    public EleBusiness getOneEleBusiness(
            HttpServletRequest request,
            @RequestParam String id
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

        return elasticsearchService.getEleBusinessById(id);
    }

    ///////////////////////////////////////////////////
    /// 获取我的店铺销量
    ///////////////////////////////////////////////////
    @GetMapping("getMyStoreVolume")
    public Integer getMyStoreVolume(
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

        EleBusiness eleBusiness = elasticsearchService.getEleBusinessById(userId);
        return eleBusiness == null ? 0 : eleBusiness.getStoreVolume();
    }

    ///////////////////////////////////////////////////
    /// 根据关键字查询商家
    ///////////////////////////////////////////////////
    @GetMapping("getEleBusinessByWord")
    public List<EleBusiness> getEleBusinessByWord(
            HttpServletRequest request,
            @RequestParam String keyword
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
        return elasticsearchService.getALLEleBusinessByKeyword(keyword,userId);
    }

    ///////////////////////////////////////////////////
    /// 创建订单
    ///////////////////////////////////////////////////
    @PostMapping("createOrder")
    public String createOrder(
            HttpServletRequest request,
            @RequestParam String id,
            @RequestBody Map<String, Integer> orderJson
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

        EleBusiness eleBusiness = elasticsearchService.getEleBusinessById(id);
        if(eleBusiness == null){
            return null;
        }

        // 计算价格创建JSON格式的订单
        Map<String, BigDecimal> items = new HashMap<>();
        BigDecimal totalPrice = new BigDecimal("3.00");
        for (EleBusiness.StoreItem storeItem : eleBusiness.getStoreItems()) {
            String itemId = storeItem.getItemId().toString();
            String itemName = storeItem.getItemName();
            Float priceFloat = storeItem.getItemPrice();

            if (priceFloat == null) continue;
            BigDecimal price = new BigDecimal(priceFloat.toString());

            if (orderJson.containsKey(itemId)) {
                Integer count = orderJson.get(itemId);
                if (count != null && count > 0) {
                    BigDecimal cnt = new BigDecimal(count.toString());
                    BigDecimal sum = price.multiply(cnt);
                    totalPrice = totalPrice.add(sum);

                    String name = itemName + " x " + count + "@" + itemId;
                    items.put(name, sum.setScale(2, RoundingMode.HALF_UP));
                }
            }
        }

        Map<String,String> res = new HashMap<>();
        res.put("storeName", eleBusiness.getStoreName());
        res.put("totalPrice", totalPrice.setScale(2, RoundingMode.HALF_UP).toString());
        res.put("items", jsonUtil.toJson(items));

        String orderInfo = jsonUtil.toJson(res);

        PayOrder payOrder = new PayOrder();
        payOrder.setUserId(JwtUtil.getUserId(accessToken).toString());
        payOrder.setOrderInfo(orderInfo);
        payOrder.setPay(false);
        if(!payOrderService.addPayOrder(payOrder)){
            log.warn("failed to add payOrder: {}", orderInfo);
            return null;
        }

        return orderInfo;
    }

}
