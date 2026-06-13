package com.be.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStoreService {
    String uploadImage(MultipartFile file);
    String updateImage(MultipartFile file, String url);
    void copyImage(String sourceKey, String destinationKey);
}
