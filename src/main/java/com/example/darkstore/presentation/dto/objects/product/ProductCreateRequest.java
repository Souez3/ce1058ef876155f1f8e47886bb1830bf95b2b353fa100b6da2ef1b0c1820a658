package com.example.darkstore.presentation.dto.objects.product;

import com.example.darkstore.dataAccess.models.CategoryEnum;
import com.example.darkstore.dataAccess.models.SubcategoryEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductCreateRequest {
    @NotBlank
    private String name;

    private String description;

    private String pictureUrl;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    private CategoryEnum category;

    private SubcategoryEnum subCategory;
}
