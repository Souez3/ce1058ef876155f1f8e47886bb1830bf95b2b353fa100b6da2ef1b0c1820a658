package com.example.darkstore.presentation.controllers

import com.example.darkstore.application.services.ProductService
import com.example.darkstore.dataAccess.models.CategoryEnum
import com.example.darkstore.dataAccess.models.SubcategoryEnum
import com.example.darkstore.presentation.dto.mappers.toEntity
import com.example.darkstore.presentation.dto.mappers.toPatch
import com.example.darkstore.presentation.dto.mappers.toResponse
import com.example.darkstore.presentation.dto.objects.product.ProductCreateRequest
import com.example.darkstore.presentation.dto.objects.product.ProductResponse
import com.example.darkstore.presentation.dto.objects.product.ProductUpdateRequest
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {

    @GetMapping
    fun search(
        @RequestParam(required = false) category: CategoryEnum?,
        @RequestParam(required = false) subcategory: SubcategoryEnum?,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @PageableDefault(size = 20, sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable,
    ): Page<ProductResponse> =
        productService.search(category, subcategory, search, minPrice, maxPrice, pageable)
            .map { it.toResponse() }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ProductResponse =
        productService.findById(id).toResponse()

    @GetMapping("/categories")
    fun categories(): List<CategoryNode> =
        CategoryEnum.entries.map { CategoryNode(it, SubcategoryEnum.byCategory(it)) }

    data class CategoryNode(val category: CategoryEnum, val subcategories: List<SubcategoryEnum>)

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: ProductCreateRequest): ProductResponse =
        productService.create(request.toEntity()).toResponse()

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    fun update(@PathVariable id: UUID, @RequestBody request: ProductUpdateRequest): ProductResponse =
        productService.update(id, request.toPatch()).toResponse()

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        productService.deleteById(id)
    }
}
