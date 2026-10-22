package com.example.darkstore.security

import com.example.darkstore.dataAccess.models.RoleEnum
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

object SecurityUtils {

    @JvmStatic
    fun getCurrentPrincipal(): UserPrincipal? =
        SecurityContextHolder.getContext().authentication?.principal as? UserPrincipal

    @JvmStatic
    fun getCurrentUserId(): UUID? = getCurrentPrincipal()?.id

    @JvmStatic
    fun hasRole(role: RoleEnum): Boolean = getCurrentPrincipal()?.user?.role == role

    @JvmStatic
    fun isStaff(): Boolean {
        val role = getCurrentPrincipal()?.user?.role ?: return false
        return role != RoleEnum.CLIENT
    }
}
