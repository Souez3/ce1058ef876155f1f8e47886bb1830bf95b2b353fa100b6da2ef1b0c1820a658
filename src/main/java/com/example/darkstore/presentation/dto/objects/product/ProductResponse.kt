package com.example.darkstore.presentation.dto.objects.product

import com.example.darkstore.dataAccess.models.CategoryEnum
import com.example.darkstore.dataAccess.models.SubcategoryEnum
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val pictureUrl: String?,
    val price: BigDecimal,
    val category: CategoryEnum,
    val subCategory: SubcategoryEnum?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)
