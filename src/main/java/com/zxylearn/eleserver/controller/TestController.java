package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.config.OssConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Value("${amap.KeySecret}")
    private String amapkeySecret;


    @GetMapping("/test")
    public String test() {
        return amapkeySecret;
    }

}
