package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.service.AmapService;
import com.zxylearn.eleserver.service.ElasticsearchService;
import com.zxylearn.eleserver.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class AmapController {


    @Autowired
    private AmapService amapService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    ///////////////////////////////////////////////////
    /// 通过经纬度获取位置文本
    ///////////////////////////////////////////////////
    @GetMapping("/getFormattedAddress")
    public String getFormattedAddress(
            HttpServletRequest request,
            @RequestParam double longitude,
            @RequestParam double latitude
    ) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return null;
        }

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
        String address = amapService.getFormattedAddress(longitude, latitude);
        if(address != null && !elasticsearchService.updateLocation(userId, longitude, latitude, address)) {
            log.error("failed to update location by lon and lat, userId={}", userId);
        }

        return address;
    }

    ///////////////////////////////////////////////////
    /// 通过位置文本获取经纬度
    ///////////////////////////////////////////////////
    @GetMapping("/getPosition")
    public Map<String, Double> getLocation(
            HttpServletRequest request,
            @RequestParam String address
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

        Map<String, Double> res = amapService.getPosition(address);
        if (res != null) {
            Double longitude = res.get("longitude");
            Double latitude = res.get("latitude");
            if (!elasticsearchService.updateLocation(userId, longitude, latitude, address)) {
                log.error("failed to update location by address, userId={}", userId);
            }
        }

        return res;
    }

}
