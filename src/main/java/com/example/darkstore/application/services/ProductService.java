package com.example.darkstore.application.services;

import com.example.darkstore.application.exceptions.EntityNotFoundException;
import com.example.darkstore.dataAccess.models.CategoryEnum;
import com.example.darkstore.dataAccess.models.Product;
import com.example.darkstore.dataAccess.models.Stock;
import com.example.darkstore.dataAccess.models.SubcategoryEnum;
import com.example.darkstore.dataAccess.repositories.OrderItemRepository;
import com.example.darkstore.dataAccess.repositories.ProductRepository;
import com.example.darkstore.dataAccess.specifications.ProductSpecifications;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final OrderItemRepository orderItemRepository;

    public Product create(@Valid Product product) {
        if (productRepository.existsByNameIgnoreCase(product.getName())) {
            throw new IllegalArgumentException("Product with name '" + product.getName() + "' already exists");
        }
        validateSubcategory(product.getCategory(), product.getSubCategory());
        Product saved = productRepository.save(product);

        stockService.initializeStockForProduct(saved);

        return saved;
    }

    @Transactional(readOnly = true)
    public Product findById(UUID productId) {
        return productRepository.findById(productId)
                .filter(p -> !p.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
    }

    @Transactional(readOnly = true)
    public Page<Product> search(CategoryEnum category,
                                SubcategoryEnum subcategory,
                                String search,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                Pageable pageable) {
        if (subcategory != null && category != null && !subcategory.belongsTo(category)) {
            throw new IllegalArgumentException(
                    "Subcategory " + subcategory + " does not belong to category " + category
            );
        }

        Specification<Product> specification = Specification.allOf(
                ProductSpecifications.notRemoved(),
                ProductSpecifications.hasCategory(category),
                ProductSpecifications.hasSubcategory(subcategory),
                ProductSpecifications.nameOrDescriptionContains(search),
                ProductSpecifications.priceGte(minPrice),
                ProductSpecifications.priceLte(maxPrice)
        );

        return productRepository.findAll(specification, pageable);
    }

    public Product update(UUID productId, Product updatedProduct) {
        Product existingProduct = findById(productId);
        if (updatedProduct.getName() != null) {

            if (productRepository.existsByNameIgnoreCase(updatedProduct.getName())
                    && !existingProduct.getName().equalsIgnoreCase(updatedProduct.getName())) {

                throw new IllegalArgumentException(
                        "Product with name '" + updatedProduct.getName() + "' already exists");
            }

            existingProduct.setName(updatedProduct.getName());
        }
        if (updatedProduct.getDescription() != null) {
            existingProduct.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getPrice() != null) {
            existingProduct.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getCategory() != null) {
            existingProduct.setCategory(updatedProduct.getCategory());
        }
        if (updatedProduct.getSubCategory() != null) {
            existingProduct.setSubCategory(updatedProduct.getSubCategory());
        }
        if (updatedProduct.getPictureUrl() != null) {
            existingProduct.setPictureUrl(updatedProduct.getPictureUrl());
        }

        validateSubcategory(existingProduct.getCategory(), existingProduct.getSubCategory());
        return productRepository.save(existingProduct);

    }

    public void deleteById(UUID productId) {
        Product product = findById(productId);
        product.setRemoved(true);
        productRepository.save(product);
    }

    private void validateSubcategory(CategoryEnum category, SubcategoryEnum subcategory) {
        if (subcategory != null && !subcategory.belongsTo(category)) {
            throw new IllegalArgumentException(
                    "Subcategory " + subcategory + " does not belong to category " + category
            );
        }
    }
}
