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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.net.URI;

@Slf4j
@Service
public class OssServiceImpl implements OssService {

    private static final List<String> validExtensions = Arrays.asList("png", "jpg", "jpeg");

    @Autowired
    private OssConfig ossConfig;

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            OSS ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            if(originalFilename == null) {
                return null;
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            if(!validExtensions.contains(extension)) {
                return null;
            }

            String fileName = "ele/" + UUID.randomUUID() + extension;
            ossClient.putObject(ossConfig.getBucketName(), fileName, inputStream);
            ossClient.shutdown();
            return "https://" + ossConfig.getBucketName() + "." +
                    ossConfig.getEndpoint() + "/" + fileName;

        } catch (Exception e) {
            log.warn("failed to upload file: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String host = uri.getHost();
            String filePath = uri.getPath().replaceFirst("^/", "");

            if (!host.equals(ossConfig.getBucketName() + "." + ossConfig.getEndpoint())) {
                log.warn("invalid bucket or endpoint in the url: {}", fileUrl);
                return false;
            }

            OSS ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            ossClient.deleteObject(ossConfig.getBucketName(), filePath);
            ossClient.shutdown();
            return true;

        } catch (URISyntaxException e) {
            log.warn("invalid url format: {}", fileUrl);
            return false;
        } catch (Exception e) {
            log.warn("failed to delete file: {}", e.getMessage());
            return false;
        }
    }

}
