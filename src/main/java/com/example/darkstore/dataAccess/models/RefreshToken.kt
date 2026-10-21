package com.example.darkstore.dataAccess.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "refresh_token")
class RefreshToken : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    @Column(nullable = false, unique = true, length = 128)
    lateinit var token: String

    @Column(nullable = false)
    lateinit var expiresAt: Instant

    @Column(nullable = false)
    var revoked: Boolean = false

    val isActive: Boolean
        get() = !revoked && expiresAt.isAfter(Instant.now())
}
