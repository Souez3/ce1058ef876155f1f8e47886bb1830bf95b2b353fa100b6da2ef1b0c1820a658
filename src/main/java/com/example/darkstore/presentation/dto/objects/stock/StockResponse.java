package com.example.darkstore.presentation.dto.objects.stock;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class StockResponse {
    private UUID productId;

    private String productName;

    private Integer quantity;

    private Integer reservedQuantity;

    private Integer availableQuantity;

    private BigDecimal purchasePrice;
}
