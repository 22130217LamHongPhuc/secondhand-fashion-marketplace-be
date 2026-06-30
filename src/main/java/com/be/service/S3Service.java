package com.be.service;

public interface S3Service {
    String uploadImageToCloud(byte[] data, String originalName);
    void updateImagetoCloud(byte[] data, String key, String contentType);
    String uploadFileStream(java.io.File file, String key, String contentType);
}