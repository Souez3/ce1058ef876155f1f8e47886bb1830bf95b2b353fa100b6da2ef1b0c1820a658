package com.example.darkstore.presentation.dto.mappers

import com.example.darkstore.application.services.ProductPatch
import com.example.darkstore.dataAccess.models.Product
import com.example.darkstore.presentation.dto.objects.product.ProductCreateRequest
import com.example.darkstore.presentation.dto.objects.product.ProductResponse
import com.example.darkstore.presentation.dto.objects.product.ProductUpdateRequest

fun ProductCreateRequest.toEntity(): Product {
    val product = Product()
    product.name = requireNotNull(name) { "name is required" }
    product.price = requireNotNull(price) { "price is required" }
    product.category = requireNotNull(category) { "category is required" }
    product.description = description
    product.pictureUrl = pictureUrl
    product.subCategory = subCategory
    return product
}

fun ProductUpdateRequest.toPatch(): ProductPatch = ProductPatch(
    name = name,
    description = description,
    pictureUrl = pictureUrl,
    price = price,
    category = category,
    subCategory = subCategory,
)

fun Product.toResponse(): ProductResponse = ProductResponse(
    id = requireNotNull(id) { "Product is not persisted" },
    name = name,
    description = description,
    pictureUrl = pictureUrl,
    price = price,
    category = category,
    subCategory = subCategory,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
