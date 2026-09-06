package com.example.darkstore.presentation.dto.objects.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private UUID id ; // new

    private UUID productId;

    private String productName;

    private Integer quantity;

    private BigDecimal priceAtPurchase;
}