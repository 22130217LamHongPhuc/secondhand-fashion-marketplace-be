package com.be.dto.response;

import com.be.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private Long parentId;
    private String parentName;
    private String name;
    private String slug;
    private String iconUrl;
    private Integer sortOrder;
    private Boolean isActive;
    private Integer childrenCount;

    public static CategoryResponse fromEntity(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .name(category.getName())
                .slug(category.getSlug())
                .iconUrl(category.getIconUrl())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .childrenCount(category.getChildren() != null ? category.getChildren().size() : 0)
                .build();
    }
}