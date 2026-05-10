package com.be.service.impl;

import com.be.service.S3Service;
import com.be.utils.KeyGeneratorUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3ServiceImplement implements S3Service {
    private final S3Client s3Client;
    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;
    @Override
    public String uploadImageToCloud(byte[] data, String originalName) {
        String contentType = determineContentType(originalName);
        String key = KeyGeneratorUtil.generateKey();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(data));
        return key;
    }

    @Override
    public void updateImagetoCloud(byte[] data, String key, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(data));
    }

    private String determineContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";

        String lowerCaseName = fileName.toLowerCase();
        if (lowerCaseName.endsWith(".pdf")) return "application/pdf";
        if (lowerCaseName.endsWith(".doc")) return "application/msword";
        if (lowerCaseName.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lowerCaseName.endsWith(".png")) return "image/png";
        if (lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg")) return "image/jpeg";

        return "application/octet-stream";
    }
}
