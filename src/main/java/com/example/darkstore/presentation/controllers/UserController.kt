package com.example.darkstore.presentation.controllers

import com.example.darkstore.application.services.UserService
import com.example.darkstore.presentation.dto.mappers.toResponse
import com.example.darkstore.presentation.dto.objects.user.ChangePasswordRequest
import com.example.darkstore.presentation.dto.objects.user.StaffCreateRequest
import com.example.darkstore.presentation.dto.objects.user.UserResponse
import com.example.darkstore.presentation.dto.objects.user.UserUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {

    @GetMapping("/me")
    fun me(): UserResponse = userService.getCurrentUser().toResponse()

    @PatchMapping("/me")
    fun updateMe(@Valid @RequestBody request: UserUpdateRequest): UserResponse =
        userService.updateCurrentUser(request).toResponse()

    @PatchMapping("/me/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest) {
        userService.changePassword(
            requireNotNull(request.currentPassword),
            requireNotNull(request.newPassword),
        )
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    fun findAll(): List<UserResponse> = userService.findAll().map { it.toResponse() }

    @PostMapping("/staff")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    fun createStaff(@Valid @RequestBody request: StaffCreateRequest): UserResponse =
        userService.createStaff(request.toRegisterRequest(), requireNotNull(request.role)).toResponse()
}
