package com.be.service;

import java.util.concurrent.CompletableFuture;

public interface ImageUploadExecutorService {
    CompletableFuture<String> uploadImage(byte[] data, String originalName);

    CompletableFuture<Void> updateImage(byte[] data, String key, String contentType);
}
