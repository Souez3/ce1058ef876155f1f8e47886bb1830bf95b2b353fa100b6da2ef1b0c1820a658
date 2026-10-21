package com.example.darkstore.dataAccess.specifications

import com.example.darkstore.dataAccess.models.CategoryEnum
import com.example.darkstore.dataAccess.models.Product
import com.example.darkstore.dataAccess.models.SubcategoryEnum
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal

object ProductSpecifications {

    fun notRemoved(): Specification<Product> =
        Specification<Product> { root, _, cb -> cb.isFalse(root.get<Boolean>("removed")) }

    fun hasCategory(category: CategoryEnum?): Specification<Product>? =
        category?.let {
            Specification<Product> { root, _, cb -> cb.equal(root.get<CategoryEnum>("category"), it) }
        }

    fun hasSubcategory(subcategory: SubcategoryEnum?): Specification<Product>? =
        subcategory?.let {
            Specification<Product> { root, _, cb -> cb.equal(root.get<SubcategoryEnum>("subCategory"), it) }
        }

    fun nameOrDescriptionContains(search: String?): Specification<Product>? {
        if (search.isNullOrBlank()) return null
        val pattern = "%" + search.trim().lowercase() + "%"
        return Specification<Product> { root, _, cb ->
            cb.or(
                cb.like(cb.lower(root.get<String>("name")), pattern),
                cb.like(cb.lower(root.get<String>("description")), pattern),
            )
        }
    }

    fun priceGte(minPrice: BigDecimal?): Specification<Product>? =
        minPrice?.let {
            Specification<Product> { root, _, cb -> cb.greaterThanOrEqualTo(root.get<BigDecimal>("price"), it) }
        }

    fun priceLte(maxPrice: BigDecimal?): Specification<Product>? =
        maxPrice?.let {
            Specification<Product> { root, _, cb -> cb.lessThanOrEqualTo(root.get<BigDecimal>("price"), it) }
        }
}
