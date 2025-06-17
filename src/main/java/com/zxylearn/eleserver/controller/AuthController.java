package com.zxylearn.eleserver.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zxylearn.eleserver.pojo.entity.EleBusiness;
import com.zxylearn.eleserver.pojo.DTO.LoginRequest;
import com.zxylearn.eleserver.pojo.DTO.RegisterRequest;
import com.zxylearn.eleserver.pojo.entity.User;
import com.zxylearn.eleserver.service.ElasticsearchService;
import com.zxylearn.eleserver.service.UserService;
import com.zxylearn.eleserver.utils.JwtUtil;
import com.zxylearn.eleserver.utils.PasswordUtil;
import com.zxylearn.eleserver.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestController
public class AuthController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private ElasticsearchService elasticsearchService;


    /// ////////////////////////////////////////////////
    /// 通过长期Jwt获取短期Jwt
    /// ////////////////////////////////////////////////
    @PostMapping("/getAccessToken")
    public ResponseEntity<Map<String, String>> getAccessToken(HttpServletRequest request) {
        Map<String, String> res = new HashMap<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.put("error", "missing or invalid authorization header");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // 检查长期Jwt令牌的合法性
        String refreshToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(refreshToken) || !JwtUtil.getType(refreshToken).equals(JwtUtil.REFRESH)) {
            res.put("error", "invalid or expired refresh token");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // 检查Jwt令牌是否在黑名单中
        String tokenId = JwtUtil.getTokenId(refreshToken);
        if (redisUtil.exists(tokenId, RedisUtil.JWT_REDIS)) {
            res.put("error", "refresh token is put on the blacklist");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        Integer userId = JwtUtil.getUserId(refreshToken);
        String email = JwtUtil.getEmail(refreshToken);
        String accessToken = JwtUtil.generateToken(userId, email, JwtUtil.ACCESS);
        res.put("accessToken", accessToken);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /// ////////////////////////////////////////////////
    /// 登录（邮箱、密码）
    /// ////////////////////////////////////////////////
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Map<String, String> res = new HashMap<>();

        String email = request.getEmail();
        String password = request.getPassword();
        String captchaImgId = request.getCaptchaImgId();
        String captchaImgText = request.getCaptchaImgText();

        // 验证图形验证码
        String captchaImgTextInRedis = redisUtil.get(captchaImgId + "@img", RedisUtil.CAPTCHA_REDIS);
        if (captchaImgTextInRedis == null || !captchaImgTextInRedis.equalsIgnoreCase(captchaImgText)) {
            res.put("errorCaptcha", "error image captcha text");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }
        redisUtil.del(captchaImgId + "@img", RedisUtil.CAPTCHA_REDIS);

        // 验证邮箱和密码
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        User user = userService.getOne(wrapper);
        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            res.put("errorEmailOrPasswd", "error email or password");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // 生成用户专属Jwt令牌
        String userRefreshToken = JwtUtil.generateToken(user.getUserId(), user.getEmail(), JwtUtil.REFRESH);

        // 返回用户信息
        EleBusiness eleBusiness = elasticsearchService.getEleBusinessById(user.getUserId().toString());
        if (eleBusiness != null && eleBusiness.getLocation() != null) {
            Point location = eleBusiness.getLocation();
            res.put("longitude", String.valueOf(location.getX()));
            res.put("latitude", String.valueOf(location.getY()));
            res.put("address", eleBusiness.getLocationText());
        }
        res.put("userRefreshToken", userRefreshToken);
        res.put("userId", user.getUserId().toString());
        res.put("avatar", user.getAvatar());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /// ////////////////////////////////////////////////
    /// 登录（长期Jwt令牌）
    /// ////////////////////////////////////////////////
    @PostMapping("/loginByRefreshToken")
    public ResponseEntity<Map<String, String>> loginByRefreshToken(HttpServletRequest request) {
        Map<String, String> res = new HashMap<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.put("error", "missing or invalid authorization header");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // 检查长期Jwt令牌的合法性
        String refreshToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(refreshToken) || !JwtUtil.getType(refreshToken).equals(JwtUtil.REFRESH)) {
            res.put("error", "invalid or expired refresh token");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // 检查Jwt令牌是否在黑名单中
        String tokenId = JwtUtil.getTokenId(refreshToken);
        if (redisUtil.exists(tokenId, RedisUtil.JWT_REDIS)) {
            res.put("error", "refresh token is put on the blacklist");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // 获取用户
        Integer userId = JwtUtil.getUserId(refreshToken);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        User user = userService.getOne(wrapper);
        if (user == null) {
            res.put("error", "failed to find user");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // 返回用户信息
        EleBusiness eleBusiness = elasticsearchService.getEleBusinessById(userId.toString());
        if (eleBusiness != null && eleBusiness.getLocation() != null) {
            Point location = eleBusiness.getLocation();
            res.put("longitude", String.valueOf(location.getX()));
            res.put("latitude", String.valueOf(location.getY()));
            res.put("address", eleBusiness.getLocationText());
        }
        res.put("userId", user.getUserId().toString());
        res.put("email", user.getEmail());
        res.put("avatar", user.getAvatar());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /// ////////////////////////////////////////////////
    /// 退出登录
    /// ////////////////////////////////////////////////
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        Map<String, String> res = new HashMap<>();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.put("error", "missing or invalid authorization header");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // 检查Jwt令牌的合法性
        String refreshToken = authHeader.substring(7);
        if (!JwtUtil.verifyToken(refreshToken) || !JwtUtil.getType(refreshToken).equals(JwtUtil.REFRESH)) {
            res.put("error", "invalid or expired refresh token");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        // 将长期Jwt令牌计入黑名单
        String tokenId = JwtUtil.getTokenId(refreshToken);
        long timeout = JwtUtil.getTokenExpirationTime(refreshToken);
        redisUtil.set(tokenId, "black", timeout, RedisUtil.JWT_REDIS);

        res.put("success", "success to logout");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /// ////////////////////////////////////////////////
    /// 注册
    /// ////////////////////////////////////////////////
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        Map<String, String> res = new HashMap<>();

        String email = request.getEmail();
        String password = request.getPassword();
        String captchaEmailId = request.getCaptchaEmailId();
        String captchaEmailText = captchaEmailId + "@" + request.getCaptchaEmailText();


        //***********************************************
        // warning
        //***********************************************
        //***********************************************
        //***********************************************
        //***********************************************
        //***********************************************
        // TODO 仅用于测试阶段
        //***********************************************
        //***********************************************
        //***********************************************
        //***********************************************
        //***********************************************
        if (!request.getCaptchaEmailText().equals("123456")) {
            // 验证邮箱验证码
            String captchaEmailTextInRedis = redisUtil.get(email, RedisUtil.CAPTCHA_REDIS);
            if (captchaEmailTextInRedis == null || !captchaEmailTextInRedis.equals(captchaEmailText)) {
                res.put("error", "error email captcha text");
                return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
            }
            redisUtil.del(email, RedisUtil.CAPTCHA_REDIS);
        }

        //验证邮箱是否被注册
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        if (userService.getOne(wrapper) != null) {
            res.put("error", "email is already registered");
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }

        //检查密码是否合法
        if (!isValidPassword(password)) {
            res.put("error", "password is invalid");
            return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
        }

        //添加用户
        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordUtil.encode(password));

        if (userService.addUser(user)) {
            res.put("success", "success to register");

            if (!elasticsearchService.createDoc(user.getUserId().toString(), email)) {
                log.error("failed to create doc, userId={}", user.getUserId());
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            res.put("error", "server mysql error");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // 判断密码是否合法
    private static final String regex = "^[A-Za-z0-9]{1,20}$";

    public static boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(password).matches();
    }
}
