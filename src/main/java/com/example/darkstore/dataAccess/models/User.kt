package com.example.darkstore.dataAccess.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

// Не data class: toString по умолчанию не печатает поля, passwordHash не утечёт в логи.
@Entity
@Table(name = "users")
class User : BaseEntity() {

    @Column(nullable = false)
    lateinit var firstName: String

    var lastName: String? = null

    @Column(unique = true)
    var email: String? = null

    @Column(unique = true, nullable = false)
    lateinit var phone: String

    @Column(name = "password_hash", nullable = false)
    lateinit var passwordHash: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: RoleEnum = RoleEnum.CLIENT

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var addresses: MutableList<DeliveryAddress> = mutableListOf()
}
