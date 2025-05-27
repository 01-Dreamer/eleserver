package com.zxylearn.eleserver.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
public class TestController {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/upload-test")
    public String uploadTestFile() {
        try {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            String content = "This is a test file uploaded from Java at " + System.currentTimeMillis();
            String fileName = "test-files/" + UUID.randomUUID() + ".txt";
            PutObjectResult result = ossClient.putObject(
                    bucketName,
                    fileName,
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))
            );
            String fileUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;
            ossClient.shutdown();

            return "文件上传成功! URL: " + fileUrl +
                    "\nETag: " + result.getETag() +
                    "\nRequestId: " + result.getRequestId();

        } catch (Exception e) {
            return "文件上传失败: " + e.getMessage();
        }
    }

    @PostMapping("/upload-custom")
    public String uploadCustomFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return "文件不能为空";
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".txt")) {
                return "仅支持上传.txt文件";
            }

            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            String fileName = "user-uploads/" + UUID.randomUUID() + ".txt";

            PutObjectResult result = ossClient.putObject(
                    bucketName,
                    fileName,
                    file.getInputStream()
            );

            String fileUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;
            ossClient.shutdown();

            return "文件上传成功! URL: " + fileUrl;

        } catch (Exception e) {
            return "文件上传失败: " + e.getMessage();
        }
    }
}