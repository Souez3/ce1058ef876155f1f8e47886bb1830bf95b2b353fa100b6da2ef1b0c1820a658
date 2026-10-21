package com.example.darkstore.dataAccess.repositories

import com.example.darkstore.dataAccess.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByPhone(phone: String): User?

    fun existsByPhone(phone: String): Boolean

    fun existsByEmailIgnoreCase(email: String): Boolean
}
