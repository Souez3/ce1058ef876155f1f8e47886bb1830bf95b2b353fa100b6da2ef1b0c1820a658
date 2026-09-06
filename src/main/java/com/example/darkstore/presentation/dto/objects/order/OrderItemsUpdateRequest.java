package com.example.darkstore.presentation.dto.objects.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderItemsUpdateRequest {

    @NotEmpty
    @Valid
    private List<@Valid OrderItemUpdateRequest> items;
}
