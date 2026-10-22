package com.example.darkstore.security

import com.example.darkstore.dataAccess.repositories.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(phone: String): UserPrincipal {
        val user = userRepository.findByPhone(phone)
            ?: throw UsernameNotFoundException("User not found: $phone")
        return UserPrincipal(user)
    }

    @Transactional(readOnly = true)
    fun loadUserById(id: UUID): UserPrincipal {
        val user = userRepository.findByIdOrNull(id)
            ?: throw UsernameNotFoundException("User not found: $id")
        return UserPrincipal(user)
    }
}
