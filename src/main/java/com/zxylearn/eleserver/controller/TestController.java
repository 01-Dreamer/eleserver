package com.zxylearn.eleserver.controller;


import com.zxylearn.eleserver.service.AmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class TestController {

    @Autowired
    private AmapService amapService;

    @GetMapping("/test")
    public Map<String, Double> test(
            @RequestParam double longitude1,
            @RequestParam double latitude1,
            @RequestParam double longitude2,
            @RequestParam double latitude2) {

        return amapService.getDistance(longitude1, latitude1, longitude2, latitude2);
    }



}