package com.example.darkstore.dataAccess.repositories;

import com.example.darkstore.dataAccess.models.Order;
import com.example.darkstore.dataAccess.models.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Order> findByCustomerPhone(String phone);
    List<Order> findByStatus(OrderStatus status);
}
