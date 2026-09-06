package com.example.darkstore.presentation.dto.objects.order;

import com.example.darkstore.dataAccess.models.OrderStatus;
import com.example.darkstore.dataAccess.models.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private UUID id;

    private String customerName;

    private String customerPhone;

    private OrderStatus status;

    private DeliveryAddressDto address;

    private String comment;

    private PaymentType paymentType;

    private BigDecimal totalPrice;

    private UUID userId;

    private List<OrderItemResponse> items;
}
