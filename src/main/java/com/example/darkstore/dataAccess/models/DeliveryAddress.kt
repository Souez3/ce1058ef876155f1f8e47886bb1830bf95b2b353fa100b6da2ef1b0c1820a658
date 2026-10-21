package com.example.darkstore.dataAccess.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "delivery_addresses")
class DeliveryAddress : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @Column(nullable = false)
    lateinit var street: String

    @Column(nullable = false)
    lateinit var house: String

    var apartment: String? = null

    @Column(nullable = false)
    lateinit var city: String

    var entrance: String? = null

    var floor: String? = null
}
