package com.example.darkstore.presentation.controllers

import com.example.darkstore.application.services.OrderService
import com.example.darkstore.dataAccess.models.OrderStatus
import com.example.darkstore.presentation.dto.mappers.toEntity
import com.example.darkstore.presentation.dto.mappers.toResponse
import com.example.darkstore.presentation.dto.objects.order.OrderCreateRequest
import com.example.darkstore.presentation.dto.objects.order.OrderItemsUpdateRequest
import com.example.darkstore.presentation.dto.objects.order.OrderResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF','COURIER') or @orderSecurity.isOwner(#id)")
    fun getById(@PathVariable id: UUID): OrderResponse =
        orderService.findById(id).toResponse()

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    fun getAll(): List<OrderResponse> =
        orderService.findAll().map { it.toResponse() }

    // Назначения курьеров пока нет, поэтому курьер может смотреть заказы по статусу
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF','COURIER')")
    fun getByStatus(@PathVariable status: OrderStatus): List<OrderResponse> =
        orderService.findByStatus(status).map { it.toResponse() }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    fun getByPhone(@PathVariable phone: String): List<OrderResponse> =
        orderService.findByPhone(phone).map { it.toResponse() }

    @GetMapping("/my")
    fun myOrders(): List<OrderResponse> =
        orderService.findMyOrders().map { it.toResponse() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: OrderCreateRequest): OrderResponse =
        orderService.create(request.toEntity()).toResponse()

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF','COURIER')")
    fun updateStatus(@PathVariable id: UUID, @RequestParam status: OrderStatus): OrderResponse =
        orderService.updateStatus(id, status).toResponse()

    @PutMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    fun updateItems(
        @PathVariable id: UUID,
        @Valid @RequestBody request: OrderItemsUpdateRequest,
    ): OrderResponse = orderService.updateItems(id, request).toResponse()

    @PatchMapping("/my/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelMyOrder(@PathVariable id: UUID) {
        orderService.cancelMyOrder(id)
    }
}
