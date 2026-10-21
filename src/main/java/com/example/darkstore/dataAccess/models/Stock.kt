package com.example.darkstore.dataAccess.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal

@Entity
@Table(name = "stock")
class Stock : BaseEntity() {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    lateinit var product: Product

    @Column(nullable = false)
    var quantity: Int = 0

    @Column(nullable = false)
    var reservedQuantity: Int = 0

    @Column(nullable = false)
    lateinit var purchasePrice: BigDecimal

    // вычисляемые остатки не хранятся в бд
    val availableQuantity: Int
        get() = quantity - reservedQuantity

    @Version
    var version: Long? = null
}
