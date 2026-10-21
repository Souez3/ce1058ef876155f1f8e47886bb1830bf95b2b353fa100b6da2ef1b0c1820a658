package com.example.darkstore.presentation.dto.objects.product

import com.example.darkstore.dataAccess.models.CategoryEnum
import com.example.darkstore.dataAccess.models.SubcategoryEnum
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class ProductCreateRequest(
    @field:NotBlank
    val name: String? = null,

    val description: String? = null,

    val pictureUrl: String? = null,

    @field:NotNull
    @field:Positive
    val price: BigDecimal? = null,

    @field:NotNull
    val category: CategoryEnum? = null,

    val subCategory: SubcategoryEnum? = null,
)
