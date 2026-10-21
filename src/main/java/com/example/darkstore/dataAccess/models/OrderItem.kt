package com.example.darkstore.dataAccess.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "order_items")
class OrderItem : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    lateinit var order: Order

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    lateinit var product: Product

    @Column(nullable = false)
    var quantity: Int = 0

    @Column(nullable = false, precision = 10, scale = 2)
    lateinit var priceAtPurchase: BigDecimal
}
