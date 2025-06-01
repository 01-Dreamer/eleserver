package com.zxylearn.eleserver.controller;


import com.zxylearn.eleserver.pojo.entity.EleBusiness;
import com.zxylearn.eleserver.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class TestController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @GetMapping("/test")
    public List<EleBusiness> test(
            @RequestParam String id
) {
        return null;
    }



}