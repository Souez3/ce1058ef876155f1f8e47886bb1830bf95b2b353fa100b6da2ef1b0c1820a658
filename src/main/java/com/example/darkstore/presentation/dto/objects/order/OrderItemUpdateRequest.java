package com.example.darkstore.presentation.dto.objects.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItemUpdateRequest {
    private UUID orderItemId;

    @NotNull
    private UUID productId;

    @NotNull
    @Positive
    private int quantity;

    private boolean remove = false;
}
