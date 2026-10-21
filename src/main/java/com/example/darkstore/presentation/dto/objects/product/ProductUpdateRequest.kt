package com.example.darkstore.presentation.dto.objects.product

import com.example.darkstore.dataAccess.models.CategoryEnum
import com.example.darkstore.dataAccess.models.SubcategoryEnum
import java.math.BigDecimal

data class ProductUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val pictureUrl: String? = null,
    val price: BigDecimal? = null,
    val category: CategoryEnum? = null,
    val subCategory: SubcategoryEnum? = null,
)
