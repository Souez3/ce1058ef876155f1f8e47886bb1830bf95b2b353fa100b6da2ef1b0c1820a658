package com.example.darkstore.security

import com.example.darkstore.dataAccess.models.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

class UserPrincipal(val user: User) : UserDetails {

    val id: UUID?
        get() = user.id

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

    override fun getPassword(): String = user.passwordHash

    override fun getUsername(): String = user.phone

    override fun isEnabled(): Boolean = !user.removed
}
