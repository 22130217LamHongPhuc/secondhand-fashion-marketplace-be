package com.be.service.impl;

import com.be.service.ImageStoreService;
import com.be.utils.KeyGeneratorUtil;
import com.be.utils.UrlGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageStorageServiceImpl implements ImageStoreService {
    private final S3Client s3Client;
    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Override
    public String uploadImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream"; // Fallback an toàn
        }
        String key = KeyGeneratorUtil.generateTempKey(file.getOriginalFilename());
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();
        try{
            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return UrlGenerator.generateUrl(key);
    }

    @Override
    public String updateImage(MultipartFile file, String url) {
        deleteFileByKey(KeyGeneratorUtil.extractKey(url));
        return uploadImage(file);
    }

    @Override
    public void copyImage(String sourceKey, String destinationKey) {
        try {
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(destinationKey)
                    .build();

            s3Client.copyObject(copyObjectRequest);
            log.info("Đã copy file thành công từ {} sang {}", sourceKey, destinationKey);
        } catch (Exception e) {
            log.error("Lỗi khi copy file từ {} sang {}: {}", sourceKey, destinationKey, e.getMessage());
            throw new RuntimeException("Không thể hoàn tất việc lưu trữ hình ảnh chính thức");
        }
    }

    @Override
    public String moveImageToTemp(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        
        try {
            String oldKey = KeyGeneratorUtil.extractKey(url);
            if (oldKey.startsWith(KeyGeneratorUtil.FOLDER_TEMP)) {
                return url;
            }
            
            String fileName = oldKey.contains("/") ? oldKey.substring(oldKey.lastIndexOf("/") + 1) : oldKey;
            String tempKey = KeyGeneratorUtil.generateTempKey(fileName);
            
            copyImage(oldKey, tempKey);
            deleteFileByKey(oldKey);
            
            return UrlGenerator.generateUrl(tempKey);
        } catch (Exception e) {
            log.error("Lỗi khi chuyển ảnh về temp cho URL [{}]: {}", url, e.getMessage());
            return null;
        }
    }

    private void deleteFileByKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Đã xóa thành công file khỏi Cloudflare R2: {}", key);

        } catch (S3Exception e) {
            log.error("Lỗi S3 khi xóa file với key [{}]: {}", key, e.awsErrorDetails().errorMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Không thể xóa file với key [{}]: {}", key, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
