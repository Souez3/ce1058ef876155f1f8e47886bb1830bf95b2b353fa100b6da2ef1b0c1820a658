package com.example.darkstore.presentation.dto.objects.stock;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockReserveRequest {
    @Positive
    private Integer quantity;
}
