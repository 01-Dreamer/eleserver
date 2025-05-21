package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class OssController {

    @Autowired
    private OssService ossService;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        return ossService.uploadImage(file);
    }
}
