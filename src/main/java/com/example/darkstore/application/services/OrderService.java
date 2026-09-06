package com.example.darkstore.application.services;

import com.example.darkstore.application.exceptions.EntityNotFoundException;
import com.example.darkstore.dataAccess.models.*;
import com.example.darkstore.dataAccess.repositories.OrderRepository;
import com.example.darkstore.dataAccess.repositories.UserRepository;
import com.example.darkstore.presentation.dto.objects.order.OrderItemUpdateRequest;
import com.example.darkstore.presentation.dto.objects.order.OrderItemsUpdateRequest;
import com.example.darkstore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final StockService stockService;
    private final ProductService productService;
    private final UserRepository userRepository;

    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found " + id));
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findMyOrders() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Not authenticated"));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return List.of();
        }
        return orderRepository.findByCustomerPhone(UserService.normalizePhone(phone));
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order create(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have items");
        }

        boolean authenticated = SecurityUtils.getCurrentUserId().isPresent();
        if (!authenticated && order.getPaymentType() == PaymentType.CASH) {
            throw new IllegalArgumentException(
                    "Cash payment is available only for authenticated users");
        }
       /* if (authenticated) {
            User user = userRepository.;

            boolean ownPhone = user.getPhone().equals(order.getCustomerPhone());

            if (!ownPhone && order.getPaymentType() == PaymentType.CASH) {
                throw new IllegalArgumentException("Cash payment is available only for orders created on user's own phone");
            }
        }*/
        // TODO: complete verification for payment type

        SecurityUtils.getCurrentUserId()
                .flatMap(userRepository::findById)
                .ifPresent(order::setUser);

        order.setCustomerPhone(UserService.normalizePhone(order.getCustomerPhone()));

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            UUID productId = item.getProduct().getId();

            Product product = productService.findById(productId);

            stockService.reserve(
                    product.getId(),
                    item.getQuantity()
            );

            item.setOrder(order);
            item.setProduct(product);
            item.setPriceAtPurchase(product.getPrice());

            total = total.add(
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        order.setTotalPrice(total);
        order.setStatus(OrderStatus.CREATED);
        return orderRepository.save(order);
    }

    @Transactional
    public void cancelMyOrder(UUID orderId) {
        Order order = findById(orderId);
        UUID currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Not authenticated"));

        if (order.getUser() == null || !currentUserId.equals(order.getUser().getId())) {
            throw new AccessDeniedException("Order does not belong to current user");
        }

        for (OrderItem item : order.getItems()) {
            stockService.cancelReservation(
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(UUID orderId, OrderStatus newStatus) {
        validateRoleForStatus(newStatus);

        Order order = findById(orderId);

        if (order.getStatus() == newStatus) {
            return order;
        }

        switch (newStatus) {
            case ASSEMBLED -> {

                if (order.getStatus() != OrderStatus.CREATED
                && order.getStatus() != OrderStatus.PAID) {
                    throw new IllegalStateException(
                            "Only CREATED orders can move to ASSEMBLED"
                    );
                }

                for (OrderItem item : order.getItems()) {
                    stockService.confirmReservation(
                            item.getProduct().getId(),
                            item.getQuantity()
                    );
                }
            }

            case CANCELLED -> {

                if (order.getStatus() == OrderStatus.SHIPPED
                        || order.getStatus() == OrderStatus.DELIVERED
                        || order.getStatus() == OrderStatus.COMPLETED
                        || order.getStatus() == OrderStatus.ASSEMBLED) {
                    throw new IllegalStateException(
                            "Cannot cancel orders that already shipped, delivered or assembled"
                    );
                }

                for (OrderItem item : order.getItems()) {
                    stockService.cancelReservation(
                            item.getProduct().getId(),
                            item.getQuantity()
                    );
                }
            }

            case PAID -> {
                if (order.getStatus() != OrderStatus.CREATED) {
                    throw new IllegalStateException(
                            "Only CREATED orders can be paid"
                    );
                }
            }

            case SHIPPED -> {
                if (order.getStatus() != OrderStatus.ASSEMBLED) {
                    throw new IllegalStateException(
                            "Only ASSEMBLED orders can be shipped"
                    );
                }
            }

            case DELIVERED -> {
                if (order.getStatus() != OrderStatus.SHIPPED) {
                    throw new IllegalStateException(
                            "Only SHIPPED orders can be delivered"
                    );
                }
            }

            case COMPLETED -> {
                if(order.getStatus() != OrderStatus.DELIVERED) {
                    throw new IllegalStateException(
                            "Only DELIVERED orders can be completed"
                    );
                }
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported status transition"
            );
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateItems(UUID orderId, OrderItemsUpdateRequest request) {
        Order order = findById(orderId);

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException(
                    "Order items can only be changed while order is CREATED or PAID, current status: "
                            + order.getStatus());
        }

        for (OrderItemUpdateRequest itemRequest : request.getItems()) {
            applyItemChange(order, itemRequest);
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        BigDecimal total = order.getItems().stream()
                .map(i -> i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);

        return orderRepository.save(order);
    }

    private void applyItemChange(Order order, OrderItemUpdateRequest request) {
        OrderItem existing = request.getOrderItemId() == null ? null :
                order.getItems().stream()
                        .filter(i -> i.getId().equals(request.getOrderItemId()))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Order item not found: " + request.getOrderItemId()));

        if (request.isRemove()) {
            if (existing == null) {
                throw new IllegalArgumentException("orderItemId is required to remove an item");
            }
            stockService.cancelReservation(existing.getProduct().getId(), existing.getQuantity());
            order.getItems().remove(existing);
            return;
        }

        Product product = productService.findById(request.getProductId());

        if (existing == null) {
            stockService.reserve(product.getId(), request.getQuantity());

            OrderItem newItem = new OrderItem();
            newItem.setOrder(order);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setPriceAtPurchase(product.getPrice());
            order.getItems().add(newItem);
            return;
        }

        boolean productChanged = !existing.getProduct().getId().equals(product.getId());

        if (productChanged) {
            stockService.cancelReservation(existing.getProduct().getId(), existing.getQuantity());
            stockService.reserve(product.getId(), request.getQuantity());

            existing.setProduct(product);
            existing.setQuantity(request.getQuantity());
            existing.setPriceAtPurchase(product.getPrice());
        } else {
            int delta = request.getQuantity() - existing.getQuantity();
            if (delta > 0) {
                stockService.reserve(product.getId(), delta);
            } else if (delta < 0) {
                stockService.cancelReservation(product.getId(), -delta);
            }
            existing.setQuantity(request.getQuantity());
        }
    }

    private void validateRoleForStatus(OrderStatus newStatus) {
        switch (newStatus) {
            case ASSEMBLED -> {
                if (!SecurityUtils.hasRole(RoleEnum.WAREHOUSE_STAFF)
                        && !SecurityUtils.hasRole(RoleEnum.MANAGER)
                        && !SecurityUtils.hasRole(RoleEnum.SYSTEM_ADMIN)) {
                    throw new AccessDeniedException("Only warehouse staff can assemble orders");
                }
            }

            case SHIPPED, DELIVERED -> {
                if (!SecurityUtils.hasRole(RoleEnum.COURIER)
                        && !SecurityUtils.hasRole(RoleEnum.MANAGER)
                        && !SecurityUtils.hasRole(RoleEnum.SYSTEM_ADMIN)) {
                    throw new AccessDeniedException("Only couriers can ship or deliver orders");
                }
            }

            case COMPLETED -> {
                if (!SecurityUtils.hasRole(RoleEnum.MANAGER)
                        && !SecurityUtils.hasRole(RoleEnum.SYSTEM_ADMIN)) {
                    throw new AccessDeniedException("Only managers can complete orders");
                }
            }

            case CANCELLED -> {
                if (!SecurityUtils.hasRole(RoleEnum.MANAGER)
                        && !SecurityUtils.hasRole(RoleEnum.SYSTEM_ADMIN)) {
                    throw new AccessDeniedException("Only managers can cancel orders");
                }
            }

            case PAID -> {
                if (!SecurityUtils.hasRole(RoleEnum.MANAGER)
                        && !SecurityUtils.hasRole(RoleEnum.SYSTEM_ADMIN)) {
                    throw new AccessDeniedException("Only managers can confirm payment");
                }
            }

            default -> {}
        }
    }
}
