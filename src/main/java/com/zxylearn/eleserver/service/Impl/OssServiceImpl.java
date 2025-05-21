package com.zxylearn.eleserver.service.Impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.zxylearn.eleserver.config.OssConfig;
import com.zxylearn.eleserver.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class OssServiceImpl implements OssService {

    @Autowired
    private OssConfig ossConfig;

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            OSS ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "ele/" + UUID.randomUUID() + extension;

            ossClient.putObject(ossConfig.getBucketName(), fileName, inputStream);
            ossClient.shutdown();

            return "https://" + ossConfig.getBucketName() + "." +
                    ossConfig.getEndpoint() + "/" + fileName;

        } catch (Exception e) {
            log.warn("failed to upload: {}", e.getMessage());
            return null;
        }
    }

}
