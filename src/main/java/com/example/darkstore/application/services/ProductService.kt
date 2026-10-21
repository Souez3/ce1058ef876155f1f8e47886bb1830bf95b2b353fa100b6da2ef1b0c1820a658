package com.example.darkstore.application.services

import com.example.darkstore.application.exceptions.EntityNotFoundException
import com.example.darkstore.dataAccess.models.CategoryEnum
import com.example.darkstore.dataAccess.models.Product
import com.example.darkstore.dataAccess.models.SubcategoryEnum
import com.example.darkstore.dataAccess.repositories.ProductRepository
import com.example.darkstore.dataAccess.specifications.ProductSpecifications
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional
@Validated
class ProductService(
    private val productRepository: ProductRepository,
    private val stockService: StockService,
) {

    fun create(@Valid product: Product): Product {
        if (productRepository.existsByNameIgnoreCase(product.name)) {
            throw IllegalArgumentException("Product with name '${product.name}' already exists")
        }
        validateSubcategory(product.category, product.subCategory)

        val saved = productRepository.save(product)
        stockService.initializeStockForProduct(saved)
        return saved
    }

    @Transactional(readOnly = true)
    fun findById(productId: UUID): Product =
        productRepository.findByIdOrNull(productId)
            ?.takeIf { !it.removed }
            ?: throw EntityNotFoundException("Product not found: $productId")

    @Transactional(readOnly = true)
    fun search(
        category: CategoryEnum?,
        subcategory: SubcategoryEnum?,
        search: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        pageable: Pageable,
    ): Page<Product> {
        if (subcategory != null && category != null && !subcategory.belongsTo(category)) {
            throw IllegalArgumentException(
                "Subcategory $subcategory does not belong to category $category"
            )
        }

        val specification: Specification<Product> = Specification.allOf(
            listOfNotNull(
                ProductSpecifications.notRemoved(),
                ProductSpecifications.hasCategory(category),
                ProductSpecifications.hasSubcategory(subcategory),
                ProductSpecifications.nameOrDescriptionContains(search),
                ProductSpecifications.priceGte(minPrice),
                ProductSpecifications.priceLte(maxPrice),
            )
        )

        return productRepository.findAll(specification, pageable)
    }

    fun update(productId: UUID, patch: ProductPatch): Product {
        val product = findById(productId)

        patch.name?.let { newName ->
            require(newName.isNotBlank()) { "Product name must not be blank" }
            if (!product.name.equals(newName, ignoreCase = true)
                && productRepository.existsByNameIgnoreCase(newName)
            ) {
                throw IllegalArgumentException("Product with name '$newName' already exists")
            }
            product.name = newName
        }
        patch.description?.let { product.description = it }
        patch.pictureUrl?.let { product.pictureUrl = it }
        patch.price?.let { newPrice ->
            require(newPrice > BigDecimal.ZERO) { "Product price must be positive" }
            product.price = newPrice
        }
        patch.category?.let { product.category = it }
        patch.subCategory?.let { product.subCategory = it }

        validateSubcategory(product.category, product.subCategory)
        return productRepository.save(product)
    }

    fun deleteById(productId: UUID) {
        val product = findById(productId)
        product.removed = true
        productRepository.save(product)
    }

    private fun validateSubcategory(category: CategoryEnum, subcategory: SubcategoryEnum?) {
        if (subcategory != null && !subcategory.belongsTo(category)) {
            throw IllegalArgumentException(
                "Subcategory $subcategory does not belong to category $category"
            )
        }
    }
}
