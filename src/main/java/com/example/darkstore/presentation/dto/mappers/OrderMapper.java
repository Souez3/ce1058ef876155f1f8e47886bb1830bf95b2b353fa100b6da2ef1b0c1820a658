package com.example.darkstore.presentation.dto.mappers;

import com.example.darkstore.dataAccess.models.Order;
import com.example.darkstore.dataAccess.models.OrderItem;
import com.example.darkstore.dataAccess.models.Product;
import com.example.darkstore.dataAccess.models.User;
import com.example.darkstore.presentation.dto.objects.order.OrderCreateRequest;
import com.example.darkstore.presentation.dto.objects.order.OrderItemResponse;
import com.example.darkstore.presentation.dto.objects.order.OrderResponse;
import com.example.darkstore.presentation.dto.objects.product.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {AddressMapper.class, OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", source = "address")
    @Mapping(target = "items", source = "items")
    Order toEntity(OrderCreateRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);
}
