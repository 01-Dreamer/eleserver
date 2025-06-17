package com.zxylearn.eleserver.controller;


import com.zxylearn.eleserver.pojo.entity.EleBusiness;
import com.zxylearn.eleserver.pojo.entity.PayOrder;
import com.zxylearn.eleserver.service.*;
import com.zxylearn.eleserver.utils.JsonUtil;
import com.zxylearn.eleserver.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
public class OrderController {
    @Autowired
    ElasticsearchService elasticsearchService;
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private JsonUtil jsonUtil;

    /// ////////////////////////////////////////////////
    /// 创建订单
    /// ////////////////////////////////////////////////
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
        if (eleBusiness == null) {
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

        Map<String, String> res = new HashMap<>();
        String orderId = UUID.randomUUID().toString();
        res.put("storeName", eleBusiness.getStoreName());
        res.put("totalPrice", totalPrice.setScale(2, RoundingMode.HALF_UP).toString());
        res.put("items", jsonUtil.toJson(items));

        String orderInfo = jsonUtil.toJson(res);

        PayOrder payOrder = new PayOrder();
        payOrder.setOrderId(orderId);
        payOrder.setUserId(JwtUtil.getUserId(accessToken));
        payOrder.setBusinessId(Integer.valueOf(id));
        payOrder.setOrderInfo(orderInfo);
        payOrder.setPay(false);
        if (!payOrderService.addPayOrder(payOrder)) {
            log.warn("failed to add payOrder: {}", orderInfo);
            return null;
        }

        // 数据库中orderInfo不存储orderId
        res.put("orderId", orderId);
        return jsonUtil.toJson(res);
    }

    /// ////////////////////////////////////////////////
    /// 获取订单
    /// ////////////////////////////////////////////////
    @GetMapping("getMyPayOrder")
    public List<PayOrder> getMyPayOrder(
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

        Integer userId = JwtUtil.getUserId(accessToken);
        return payOrderService.getPayOrderByUserId(userId);
    }

    /// ////////////////////////////////////////////////
    /// 支付订单
    /// ////////////////////////////////////////////////
    @PostMapping("submitPayOrder")
    public boolean submitPayOrder(
            HttpServletRequest request,
            @RequestParam String orderId
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

        Integer userId = JwtUtil.getUserId(accessToken);
        Integer businessId = payOrderService.submitPayOrder(userId, orderId);
        if (businessId != null) {
            elasticsearchService.addStoreVolume(businessId.toString());
            return true;
        }

        return false;
    }

    /// ////////////////////////////////////////////////
    /// 提交评价
    /// ////////////////////////////////////////////////
    @PostMapping("submitReview")
    public boolean submitReview(
            HttpServletRequest request,
            @RequestParam String orderId,
            @RequestParam Integer review
    ) {
        // 检查评价是否合法
        if(review < 0 || review > 10) {
            return false;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        // 检查短期Jwt令牌的合法性
        String accessToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(accessToken) || !JwtUtil.getType(accessToken).equals(JwtUtil.ACCESS)) {
            return false;
        }

        Integer userId = JwtUtil.getUserId(accessToken);
        Integer businessId = payOrderService.submitReview(userId, orderId, review);
        if (businessId != null) {
            elasticsearchService.updateStoreReview(businessId.toString(), (float) (review / 2.0));
            return true;
        }

        return false;
    }
}
