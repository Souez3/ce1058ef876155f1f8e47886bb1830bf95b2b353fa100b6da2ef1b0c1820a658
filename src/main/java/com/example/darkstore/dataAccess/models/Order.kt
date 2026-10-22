package com.example.darkstore.dataAccess.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "orders")
class Order : BaseEntity() {

    var customerName: String? = null

    @Column(nullable = false)
    lateinit var customerPhone: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.CREATED

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(nullable = false)
    lateinit var address: DeliveryAddress

    var comment: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var paymentType: PaymentType = PaymentType.CASH

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableList<OrderItem> = mutableListOf()

    @Column(nullable = false, precision = 10, scale = 2)
    lateinit var totalPrice: BigDecimal

    // null для гостевых заказов
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null
}
