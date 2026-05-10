package com.be.service.impl;

import com.be.service.ImageUploadExecutorService;
import com.be.service.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class ImageUploadExecutorServiceImpl implements ImageUploadExecutorService {

    private final S3Service s3Service;
    private final ExecutorService imageUploadExecutorService;

    public ImageUploadExecutorServiceImpl(
            S3Service s3Service,
            @Qualifier("imageUploadExecutorService") ExecutorService imageUploadExecutorService
    ) {
        this.s3Service = s3Service;
        this.imageUploadExecutorService = imageUploadExecutorService;
    }

    @Override
    public CompletableFuture<String> uploadImage(byte[] data, String originalName) {
        return CompletableFuture.supplyAsync(
                () -> s3Service.uploadImageToCloud(data, originalName),
                imageUploadExecutorService
        );
    }

    @Override
    public CompletableFuture<Void> updateImage(byte[] data, String key, String contentType) {
        return CompletableFuture.runAsync(
                () -> s3Service.updateImagetoCloud(data, key, contentType),
                imageUploadExecutorService
        );
    }
}
