package com.example.darkstore.presentation.controllers

import com.example.darkstore.application.services.StockService
import com.example.darkstore.presentation.dto.mappers.toResponse
import com.example.darkstore.presentation.dto.objects.stock.StockAddRequest
import com.example.darkstore.presentation.dto.objects.stock.StockResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/stock")
class StockController(
    private val stockService: StockService,
) {

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    fun getStock(@PathVariable productId: UUID): StockResponse =
        stockService.getStockByProductId(productId).toResponse()

    @PostMapping("/{productId}/add")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addStock(@PathVariable productId: UUID, @Valid @RequestBody request: StockAddRequest) {
        stockService.addStock(
            productId,
            requireNotNull(request.quantity) { "quantity is required" },
            request.purchasePrice,
        )
    }
}
