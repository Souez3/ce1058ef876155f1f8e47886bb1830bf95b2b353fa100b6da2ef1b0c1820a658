package com.example.darkstore.presentation.dto.objects.stock;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StockAddRequest {
    @Positive
    private Integer quantity;

    @PositiveOrZero
    private BigDecimal purchasePrice;
}
