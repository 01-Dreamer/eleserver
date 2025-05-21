package com.zxylearn.eleserver.service;


import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    String uploadFile(MultipartFile file);
    boolean deleteFile(String fileUrl);
}
