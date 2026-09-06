package com.example.darkstore.presentation.dto.objects.product;

import com.example.darkstore.dataAccess.models.CategoryEnum;
import com.example.darkstore.dataAccess.models.SubcategoryEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ProductResponse {
    private UUID id;

    private String name;

    private String description;

    private String pictureUrl;

    private BigDecimal price;

    private CategoryEnum category;

    private SubcategoryEnum subCategory;

    private Instant createdAt;

    private Instant updatedAt;
}
