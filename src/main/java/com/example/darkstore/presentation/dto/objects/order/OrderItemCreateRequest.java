package com.example.darkstore.presentation.dto.objects.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItemCreateRequest {
    @NotNull
    private UUID productId;

    @Positive
    private Integer quantity;
}
