package com.zxylearn.eleserver.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zxylearn.eleserver.mapper.UserMapper;
import com.zxylearn.eleserver.pojo.LoginRequest;
import com.zxylearn.eleserver.pojo.User;
import com.zxylearn.eleserver.service.UserService;
import com.zxylearn.eleserver.utils.JwtUtil;
import com.zxylearn.eleserver.utils.RedisUtil;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
public class AuthController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        System.out.println(request.toString());

        Map<String, String> res = new HashMap<>();

        String email = request.getEmail();
        String password = request.getPassword();
        String captchaImgId = request.getCaptchaImgId();
        String captchaImgText = request.getCaptchaImgText();

        // 验证图形验证码
        String captchaImgTextInRedis = redisUtil.get(captchaImgId + "@img", RedisUtil.CAPTCHA_REDIS);
        if(captchaImgTextInRedis == null || !captchaImgTextInRedis.equalsIgnoreCase(captchaImgText)) {
            res.put("error_", "error image captcha text");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

        // 验证邮箱和密码
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        User user = userService.getOne(wrapper);
        if(user == null || !user.getPassword().equals(password)) {
            res.put("error", "error email or password");
            return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }





    private static final String regex = "^[A-Za-z0-9]{1,20}$";
    public static boolean isValidPassword(String input) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(input).matches();
    }









//    @GetMapping("/accessJwt")
//    public String accessJwt() {
//        return JwtUtil.generateToken(Integer.valueOf("1"), "2711339704@qq.com", JwtUtil.ACCESS);
//    }
//
//    @GetMapping("verifyJwt")
//    public String verifyJwt(@RequestParam String token) {
//        if (JwtUtil.verifyToken(token)) {
//            return "success";
//        }
//        return "fail";
//    }
//
//    @GetMapping("parseJwt")
//    public String parseJwt(@RequestParam String token) {
//        return JwtUtil.getEmail(token);
//    }
//

}
