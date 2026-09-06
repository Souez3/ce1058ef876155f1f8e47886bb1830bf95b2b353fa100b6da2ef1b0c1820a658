package com.example.darkstore.dataAccess.specifications;

import com.example.darkstore.dataAccess.models.CategoryEnum;
import com.example.darkstore.dataAccess.models.Product;
import com.example.darkstore.dataAccess.models.SubcategoryEnum;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> notRemoved() {
        return (root, query, cb) -> cb.isFalse(root.get("removed"));
    }

    public static Specification<Product> hasCategory(CategoryEnum category) {
        if (category == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Product> hasSubcategory(SubcategoryEnum subcategory) {
        if (subcategory == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("subCategory"), subcategory);
    }

    public static Specification<Product> nameOrDescriptionContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    public static Specification<Product> priceGte(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLte(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}
