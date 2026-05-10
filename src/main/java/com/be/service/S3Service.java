package com.be.service;

public interface S3Service {
    String uploadImageToCloud(byte[] data, String originalName);
    void updateImagetoCloud(byte[] data, String key, String contentType);
}
