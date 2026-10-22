package com.example.darkstore.application.services

import com.example.darkstore.application.exceptions.EntityNotFoundException
import com.example.darkstore.dataAccess.models.Order
import com.example.darkstore.dataAccess.models.OrderItem
import com.example.darkstore.dataAccess.models.OrderStatus
import com.example.darkstore.dataAccess.models.PaymentType
import com.example.darkstore.dataAccess.models.RoleEnum
import com.example.darkstore.dataAccess.repositories.OrderRepository
import com.example.darkstore.dataAccess.repositories.UserRepository
import com.example.darkstore.presentation.dto.objects.order.OrderItemUpdateRequest
import com.example.darkstore.presentation.dto.objects.order.OrderItemsUpdateRequest
import com.example.darkstore.security.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val stockService: StockService,
    private val productService: ProductService,
    private val userRepository: UserRepository,
) {

    private val log = LoggerFactory.getLogger(OrderService::class.java)

    fun findById(id: UUID): Order =
        orderRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Order not found $id")

    fun findAll(): List<Order> = orderRepository.findAll()

    fun findMyOrders(): List<Order> {
        val userId = currentUserIdOrNull() ?: throw AccessDeniedException("Not authenticated")
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    fun findByPhone(phone: String?): List<Order> {
        if (phone.isNullOrBlank()) return emptyList()
        return orderRepository.findByCustomerPhone(UserService.normalizePhone(phone))
    }

    fun findByStatus(status: OrderStatus): List<Order> = orderRepository.findByStatus(status)

    @Transactional
    fun create(order: Order): Order {
        require(order.items.isNotEmpty()) { "Order must have items" }

        val currentUserId = currentUserIdOrNull()
        order.customerPhone = UserService.normalizePhone(order.customerPhone)

        require(currentUserId != null || order.paymentType != PaymentType.CASH) {
            "Cash payment is available only for authenticated users"
        }

        if (currentUserId != null) {
            val currentUser = userRepository.findByIdOrNull(currentUserId)
                ?: throw AccessDeniedException("User not found")
            require(order.paymentType != PaymentType.CASH || currentUser.phone == order.customerPhone) {
                "Cash payment is available only for the account's own phone number"
            }
            order.user = currentUser
        }

        var total = BigDecimal.ZERO
        for (item in order.items) {
            val product = productService.findById(requireNotNull(item.product.id) { "productId is required" })

            stockService.reserve(requireNotNull(product.id), item.quantity)

            item.order = order
            item.product = product
            item.priceAtPurchase = product.price

            total += product.price * BigDecimal.valueOf(item.quantity.toLong())
        }

        order.totalPrice = total
        order.status = OrderStatus.CREATED
        val saved = orderRepository.save(order)
        log.info("Order {} created: items={}, total={}, paymentType={}", saved.id, saved.items.size, total, saved.paymentType)
        return saved
    }

    @Transactional
    fun cancelMyOrder(orderId: UUID) {
        val order = findById(orderId)
        val currentUserId = currentUserIdOrNull() ?: throw AccessDeniedException("Not authenticated")

        if (order.user?.id != currentUserId) {
            log.warn("User {} tried to cancel foreign order {}", currentUserId, orderId)
            throw AccessDeniedException("Order does not belong to current user")
        }

        releaseReservations(order)
        order.status = OrderStatus.CANCELLED
        orderRepository.save(order)
        log.info("Order {} cancelled by owner {}", orderId, currentUserId)
    }

    @Transactional
    fun updateStatus(orderId: UUID, newStatus: OrderStatus): Order {
        validateRoleForStatus(newStatus)

        val order = findById(orderId)
        if (order.status == newStatus) return order

        when (newStatus) {
            OrderStatus.ASSEMBLED -> {
                check(order.status == OrderStatus.CREATED || order.status == OrderStatus.PAID) {
                    "Only CREATED orders can move to ASSEMBLED"
                }
                for (item in order.items) {
                    stockService.confirmReservation(requireNotNull(item.product.id), item.quantity)
                }
            }

            OrderStatus.CANCELLED -> {
                check(
                    order.status != OrderStatus.SHIPPED
                            && order.status != OrderStatus.DELIVERED
                            && order.status != OrderStatus.COMPLETED
                            && order.status != OrderStatus.ASSEMBLED
                ) { "Cannot cancel orders that already shipped, delivered or assembled" }
                releaseReservations(order)
            }

            OrderStatus.PAID ->
                check(order.status == OrderStatus.CREATED) { "Only CREATED orders can be paid" }

            OrderStatus.SHIPPED ->
                check(order.status == OrderStatus.ASSEMBLED) { "Only ASSEMBLED orders can be shipped" }

            OrderStatus.DELIVERED ->
                check(order.status == OrderStatus.SHIPPED) { "Only SHIPPED orders can be delivered" }

            OrderStatus.COMPLETED ->
                check(order.status == OrderStatus.DELIVERED) { "Only DELIVERED orders can be completed" }

            else -> throw IllegalArgumentException("Unsupported status transition")
        }

        order.status = newStatus
        val saved = orderRepository.save(order)
        log.info("Order {} status changed to {}", orderId, newStatus)
        return saved
    }

    @Transactional
    fun updateItems(orderId: UUID, request: OrderItemsUpdateRequest): Order {
        val order = findById(orderId)

        check(order.status == OrderStatus.CREATED || order.status == OrderStatus.PAID) {
            "Order items can only be changed while order is CREATED or PAID, current status: ${order.status}"
        }

        for (itemRequest in request.items.orEmpty()) {
            applyItemChange(order, itemRequest)
        }

        require(order.items.isNotEmpty()) { "Order must have at least one item" }

        order.totalPrice = order.items.fold(BigDecimal.ZERO) { acc, i ->
            acc + i.priceAtPurchase * BigDecimal.valueOf(i.quantity.toLong())
        }

        val saved = orderRepository.save(order)
        log.info("Order {} items updated, new total={}", orderId, saved.totalPrice)
        return saved
    }

    private fun applyItemChange(order: Order, request: OrderItemUpdateRequest) {
        val existing: OrderItem? = request.orderItemId?.let { itemId ->
            order.items.firstOrNull { it.id == itemId }
                ?: throw EntityNotFoundException("Order item not found: $itemId")
        }

        if (request.remove) {
            requireNotNull(existing) { "orderItemId is required to remove an item" }
            stockService.cancelReservation(requireNotNull(existing.product.id), existing.quantity)
            order.items.remove(existing)
            return
        }

        val product = productService.findById(requireNotNull(request.productId) { "productId is required" })
        val productId = requireNotNull(product.id)

        if (existing == null) {
            stockService.reserve(productId, request.quantity)

            val newItem = OrderItem()
            newItem.order = order
            newItem.product = product
            newItem.quantity = request.quantity
            newItem.priceAtPurchase = product.price
            order.items.add(newItem)
            return
        }

        if (existing.product.id != productId) {
            stockService.cancelReservation(requireNotNull(existing.product.id), existing.quantity)
            stockService.reserve(productId, request.quantity)

            existing.product = product
            existing.quantity = request.quantity
            existing.priceAtPurchase = product.price
        } else {
            val delta = request.quantity - existing.quantity
            when {
                delta > 0 -> stockService.reserve(productId, delta)
                delta < 0 -> stockService.cancelReservation(productId, -delta)
            }
            existing.quantity = request.quantity
        }
    }

    private fun releaseReservations(order: Order) {
        for (item in order.items) {
            stockService.cancelReservation(requireNotNull(item.product.id), item.quantity)
        }
    }

    private fun currentUserIdOrNull(): UUID? = SecurityUtils.getCurrentUserId()

    private fun validateRoleForStatus(newStatus: OrderStatus) {
        val manager = SecurityUtils.hasRole(RoleEnum.MANAGER) || SecurityUtils.hasRole(RoleEnum.SYSTEM_ADMIN)

        when (newStatus) {
            OrderStatus.ASSEMBLED ->
                if (!manager && !SecurityUtils.hasRole(RoleEnum.WAREHOUSE_STAFF)) {
                    throw AccessDeniedException("Only warehouse staff can assemble orders")
                }

            OrderStatus.SHIPPED, OrderStatus.DELIVERED ->
                if (!manager && !SecurityUtils.hasRole(RoleEnum.COURIER)) {
                    throw AccessDeniedException("Only couriers can ship or deliver orders")
                }

            OrderStatus.COMPLETED ->
                if (!manager) throw AccessDeniedException("Only managers can complete orders")

            OrderStatus.CANCELLED ->
                if (!manager) throw AccessDeniedException("Only managers can cancel orders")

            OrderStatus.PAID ->
                if (!manager) throw AccessDeniedException("Only managers can confirm payment")

            else -> Unit
        }
    }
}
