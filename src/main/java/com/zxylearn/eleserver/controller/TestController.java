package com.zxylearn.eleserver.controller;


import com.zxylearn.eleserver.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @GetMapping("/test")
    public String test() {
        elasticsearchService.updateStoreCover("1", "url");
        elasticsearchService.updateStoreVolume("1", 1);
        elasticsearchService.updateLocation("1", 0.0, 0.0);
        elasticsearchService.updateLocationText("1", "云南大学软件学院");
        elasticsearchService.addStoreItem("1", "饺子","好吃","url", 99.0F);
        return "test";
    }


}