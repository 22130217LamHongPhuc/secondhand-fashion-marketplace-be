package com.be.controller.images;

import com.be.dto.response.ApiResponse;
import com.be.service.ImageStoreService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/images")
@AllArgsConstructor
public class ImageController {
    private final ImageStoreService imageStoreService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    public ResponseEntity<ApiResponse<String>> post(@RequestParam("file") MultipartFile file) {
        if(file.isEmpty()){
            throw new IllegalArgumentException("Image is empty");
        }
        var url = imageStoreService.uploadImage(file);
        return ResponseEntity.ok(new ApiResponse<>(url,"uploaded"));
    }
}
