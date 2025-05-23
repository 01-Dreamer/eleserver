package com.zxylearn.eleserver.controller;


import com.zxylearn.eleserver.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private ElasticsearchService esService;

    @GetMapping("/test")
    public boolean test() {
        return esService.delStoreItem("1","饺子");
    }


}