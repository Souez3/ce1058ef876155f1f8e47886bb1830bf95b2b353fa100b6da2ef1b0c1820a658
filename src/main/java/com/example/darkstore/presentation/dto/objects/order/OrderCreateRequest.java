package com.example.darkstore.presentation.dto.objects.order;

import com.example.darkstore.dataAccess.models.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {
    private String customerName;

    @NotBlank
    private String customerPhone;

    private String comment;

    @NotNull
    private PaymentType paymentType;

    @NotNull
    private DeliveryAddressDto address;

    @NotEmpty
    private List<OrderItemCreateRequest> items;
}
