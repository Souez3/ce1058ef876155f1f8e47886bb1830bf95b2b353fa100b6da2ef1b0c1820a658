package com.example.darkstore.application.services

import com.example.darkstore.application.exceptions.EntityNotFoundException
import com.example.darkstore.dataAccess.models.RoleEnum
import com.example.darkstore.dataAccess.models.User
import com.example.darkstore.dataAccess.repositories.UserRepository
import com.example.darkstore.presentation.dto.objects.auth.RegisterRequest
import com.example.darkstore.presentation.dto.objects.user.UserUpdateRequest
import com.example.darkstore.security.SecurityUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    companion object {
        // "8 (912) 345-67-89" -> "+79123456789
        fun normalizePhone(raw: String): String {
            var digits = raw.replace(Regex("[^\\d+]"), "")
            digits = when {
                digits.startsWith("8") && digits.length == 11 -> "+7" + digits.substring(1)
                !digits.startsWith("+") -> "+$digits"
                else -> digits
            }
            return digits
        }
    }

    fun findById(id: UUID): User =
        userRepository.findByIdOrNull(id) ?: throw EntityNotFoundException("User not found: $id")

    fun getCurrentUser(): User {
        val userId = SecurityUtils.getCurrentUserId()
            ?: throw AccessDeniedException("Not authenticated")
        return findById(userId)
    }

    fun findAll(): List<User> = userRepository.findAll()

    @Transactional
    fun registerClient(request: RegisterRequest): User {
        val phone = normalizePhone(requireNotNull(request.phone) { "phone is required" })
        val email = request.email?.trim()?.takeIf { it.isNotEmpty() }

        require(!userRepository.existsByPhone(phone)) { "User with this phone already exists" }
        if (email != null) {
            require(!userRepository.existsByEmailIgnoreCase(email)) { "User with this email already exists" }
        }

        val user = User()
        user.firstName = requireNotNull(request.firstName) { "firstName is required" }
        user.lastName = request.lastName
        user.phone = phone
        user.email = email
        user.passwordHash = passwordEncoder.encode(requireNotNull(request.password) { "password is required" })
        user.role = RoleEnum.CLIENT

        return userRepository.save(user)
    }

    @Transactional
    fun createStaff(request: RegisterRequest, role: RoleEnum): User {
        require(role != RoleEnum.CLIENT) { "Use register endpoint for clients" }
        val user = registerClient(request)
        user.role = role
        return userRepository.save(user)
    }

    @Transactional
    fun updateCurrentUser(request: UserUpdateRequest): User {
        val user = getCurrentUser()

        request.firstName?.takeIf { it.isNotBlank() }?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }

        val newEmail = request.email?.trim()
        if (!newEmail.isNullOrEmpty() && !newEmail.equals(user.email, ignoreCase = true)) {
            require(!userRepository.existsByEmailIgnoreCase(newEmail)) { "Email already in use" }
            user.email = newEmail
        }
        return userRepository.save(user)
    }

    @Transactional
    fun changePassword(currentPassword: String, newPassword: String) {
        val user = getCurrentUser()
        require(passwordEncoder.matches(currentPassword, user.passwordHash)) { "Current password is incorrect" }
        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)
    }
}
