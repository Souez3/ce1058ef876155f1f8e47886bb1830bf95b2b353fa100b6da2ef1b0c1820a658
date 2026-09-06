package com.example.darkstore.presentation.dto.objects.product;

import com.example.darkstore.dataAccess.models.CategoryEnum;
import com.example.darkstore.dataAccess.models.SubcategoryEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductUpdateRequest {
    private String name;

    private String description;

    private String pictureUrl;

    private BigDecimal price;

    private CategoryEnum category;

    private SubcategoryEnum subCategory;
}
