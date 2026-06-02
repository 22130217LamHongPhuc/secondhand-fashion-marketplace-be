package com.be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {
    private Long parentId;
    private String name;
    private String slug;
    private String iconUrl;
    private Integer sortOrder;
    private Boolean isActive;
}