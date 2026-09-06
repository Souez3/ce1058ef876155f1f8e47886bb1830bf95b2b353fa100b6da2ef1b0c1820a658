package com.example.darkstore.security;

import com.example.darkstore.dataAccess.models.Order;
import com.example.darkstore.dataAccess.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(UUID orderId) {
        return SecurityUtils.getCurrentUserId()
                .map(userId -> orderRepository.findById(orderId)
                        .map(Order::getUser)
                        .map(u -> userId.equals(u.getId()))
                        .orElse(false))
                .orElse(false);
    }
}
