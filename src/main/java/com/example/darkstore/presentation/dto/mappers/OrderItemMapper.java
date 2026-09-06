package com.example.darkstore.presentation.dto.mappers;

import com.example.darkstore.dataAccess.models.OrderItem;
import com.example.darkstore.dataAccess.models.Product;
import com.example.darkstore.presentation.dto.objects.order.OrderItemCreateRequest;
import com.example.darkstore.presentation.dto.objects.order.OrderItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", source = "productId")
    @Mapping(target = "priceAtPurchase", ignore = true)
    OrderItem toEntity(OrderItemCreateRequest request);

    default Product map(UUID id) {
        if (id == null) {
            return null;
        }

        Product product = new Product();
        product.setId(id);
        return product;
    }

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderItemResponse toResponse(OrderItem item);

    List<OrderItemResponse> toResponseList(List<OrderItem> items);
}
