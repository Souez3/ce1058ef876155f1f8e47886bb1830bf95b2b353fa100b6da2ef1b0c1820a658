package com.example.darkstore.presentation.controllers;

import com.example.darkstore.application.services.ProductService;
import com.example.darkstore.dataAccess.models.CategoryEnum;
import com.example.darkstore.dataAccess.models.Product;
import com.example.darkstore.dataAccess.models.SubcategoryEnum;
import com.example.darkstore.presentation.dto.mappers.ProductMapper;
import com.example.darkstore.presentation.dto.objects.product.ProductCreateRequest;
import com.example.darkstore.presentation.dto.objects.product.ProductResponse;
import com.example.darkstore.presentation.dto.objects.product.ProductUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public Page<ProductResponse> search(
            @RequestParam(required = false) CategoryEnum category,
            @RequestParam(required = false)SubcategoryEnum subcategory,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return productService.search(category, subcategory, search, minPrice, maxPrice, pageable)
                .map(productMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @GetMapping("/categories")
    public List<CategoryNode> categories() {
        return java.util.Arrays.stream(CategoryEnum.values())
                .map(c -> new CategoryNode(c, SubcategoryEnum.byCategory(c)))
                .toList();
    }

    public record CategoryNode(CategoryEnum category, List<SubcategoryEnum> subcategories) {}

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        Product created = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @RequestBody ProductUpdateRequest request) {

        Product product = productMapper.toEntity(request);

        Product updated = productService.update(id, product);

        return ResponseEntity.ok(
                productMapper.toResponse(updated)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
