package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.service.AmapService;
import com.zxylearn.eleserver.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AmapController {


    @Autowired
    private AmapService amapService;

    @GetMapping("/getFormattedAddress")
    public String getFormattedAddress(
            HttpServletRequest request,
            @RequestParam double longitude,
            @RequestParam double latitude
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

        return amapService.getFormattedAddress(longitude, latitude);
    }

}
