package com.zxylearn.eleserver.controller;


import com.zxylearn.eleserver.utils.CaptchaUtil;
import com.zxylearn.eleserver.utils.EmailUtil;
import com.zxylearn.eleserver.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class CaptchaController {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    EmailUtil emailUtil;


    @GetMapping("/captchaImg")
    public ResponseEntity<byte[]> getCaptchaImg() throws IOException {
        String imgCaptchaText = CaptchaUtil.generateRandomCaptchaText(CaptchaUtil.IMG_CAPTCHA_CHAR);
        String imgCaptchaId = UUID.randomUUID().toString();

        BufferedImage imgCaptcha = CaptchaUtil.generateCaptchaImage(imgCaptchaText);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imgCaptcha, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.add("imgCaptchaId", imgCaptchaId);
        redisUtil.set(imgCaptchaId + "@img", imgCaptchaText, 180, RedisUtil.CAPTCHA_REDIS);

        return ResponseEntity.ok().headers(headers).body(imageBytes);
    }


    @GetMapping("/captchaEmail")
    public ResponseEntity<Map<String, String>> getCaptchaEmail(@RequestParam String email) {
        Map<String, String> res = new HashMap<>();

        long wait = redisUtil.ttl(email, RedisUtil.CAPTCHA_REDIS);
        if (wait > 0) {
            res.put("wait", String.valueOf(wait));
            return new ResponseEntity<>(res, HttpStatus.TOO_MANY_REQUESTS);
        }


        String emailCaptchaText = CaptchaUtil.generateRandomCaptchaText(CaptchaUtil.EMAIL_CAPTCHA_CHAR);
        String emailCaptchaId = UUID.randomUUID().toString();
        if (!emailUtil.send(email, "软工实训饿了么验证码", emailCaptchaText + ",验证码请勿泄露,有效时间为180秒。")) {
            res.put("error", "failed to send");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        res.put("emailCaptchaId", emailCaptchaId);
        redisUtil.set(email, emailCaptchaId + "@" + emailCaptchaText, 180, RedisUtil.CAPTCHA_REDIS);


        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
