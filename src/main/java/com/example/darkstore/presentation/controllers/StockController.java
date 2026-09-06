package com.example.darkstore.presentation.controllers;

import com.example.darkstore.application.services.StockService;
import com.example.darkstore.dataAccess.models.Stock;
import com.example.darkstore.presentation.dto.mappers.StockMapper;
import com.example.darkstore.presentation.dto.objects.stock.StockAddRequest;
import com.example.darkstore.presentation.dto.objects.stock.StockResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/stock")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;
    private final StockMapper stockMapper;

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    public ResponseEntity<StockResponse> getStock(
            @PathVariable UUID productId) {

        Stock stock = stockService.getStockByProductId(productId);

        return ResponseEntity.ok(stockMapper.toResponse(stock));
    }

    @PostMapping("/{productId}/add")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','MANAGER','WAREHOUSE_STAFF')")
    public ResponseEntity<Void> addStock(
            @PathVariable UUID productId,
            @Valid @RequestBody StockAddRequest request) {

        stockService.addStock(
                productId,
                request.getQuantity(),
                request.getPurchasePrice()
        );

        return ResponseEntity.noContent().build();
    }
}
