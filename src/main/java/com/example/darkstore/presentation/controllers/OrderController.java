package com.example.darkstore.presentation.controllers;

import com.example.darkstore.application.services.OrderService;
import com.example.darkstore.dataAccess.models.Order;
import com.example.darkstore.dataAccess.models.OrderStatus;
import com.example.darkstore.presentation.dto.mappers.OrderMapper;
import com.example.darkstore.presentation.dto.objects.order.OrderCreateRequest;
import com.example.darkstore.presentation.dto.objects.order.OrderItemsUpdateRequest;
import com.example.darkstore.presentation.dto.objects.order.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF','COURIER')"
    + "or @orderSecurity.isOwner(#id)")
    public OrderResponse getById(@PathVariable UUID id) {
        return orderMapper.toResponse(orderService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    public List<OrderResponse> getAll() {
        return orderMapper.toResponseList(orderService.findAll());
    }

    // No assignment of courier at current version so courier allowed to getByStatus rn
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF','COURIER')")
    public List<OrderResponse> getByStatus(@PathVariable OrderStatus status) {
        return orderMapper.toResponseList(orderService.findByStatus(status));
    }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    public List<OrderResponse> getByPhone(@PathVariable String phone) {
        return orderMapper.toResponseList(orderService.findByPhone(phone));
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders() {
        return orderMapper.toResponseList(orderService.findMyOrders());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderMapper.toEntity(request);
        return orderMapper.toResponse(orderService.create(order));
    } // TODO: forbid setting payment type CASH for guest orders

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF','COURIER')")
    public OrderResponse updateStatus(@PathVariable UUID id, @RequestParam OrderStatus status) {
        return orderMapper.toResponse(orderService.updateStatus(id, status));
    }

    @PutMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    public OrderResponse updateItems(
            @PathVariable UUID id,
            @Valid @RequestBody OrderItemsUpdateRequest request
    ) {
        return orderMapper.toResponse(
                orderService.updateItems(id, request)
        );
    }

    @PatchMapping("/my/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelMyOrder(@PathVariable UUID id) {
        orderService.cancelMyOrder(id);
    }
}
