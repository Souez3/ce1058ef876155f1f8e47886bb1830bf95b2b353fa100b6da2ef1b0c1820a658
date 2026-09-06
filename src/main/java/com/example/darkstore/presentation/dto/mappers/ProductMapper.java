package com.example.darkstore.presentation.dto.mappers;

import com.example.darkstore.dataAccess.models.Product;
import com.example.darkstore.presentation.dto.objects.product.ProductCreateRequest;
import com.example.darkstore.presentation.dto.objects.product.ProductResponse;
import com.example.darkstore.presentation.dto.objects.product.ProductUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toEntity(ProductCreateRequest request);
    ProductResponse toResponse(Product product);
    List<ProductResponse> toResponseList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removed", ignore = true)
    Product toEntity(ProductUpdateRequest request);
}
