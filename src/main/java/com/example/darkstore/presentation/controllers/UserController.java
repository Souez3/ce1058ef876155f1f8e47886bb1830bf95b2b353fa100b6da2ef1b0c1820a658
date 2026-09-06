package com.example.darkstore.presentation.controllers;

import com.example.darkstore.application.services.UserService;
import com.example.darkstore.presentation.dto.mappers.UserMapper;
import com.example.darkstore.presentation.dto.objects.user.ChangePasswordRequest;
import com.example.darkstore.presentation.dto.objects.user.StaffCreateRequest;
import com.example.darkstore.presentation.dto.objects.user.UserResponse;
import com.example.darkstore.presentation.dto.objects.user.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserResponse me() {
        return userMapper.toResponse(userService.getCurrentUser());
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UserUpdateRequest request) {
        return userMapper.toResponse(userService.updateCurrentUser(request));
    }

    @PatchMapping("/me/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request.getCurrentPassword(), request.getNewPassword());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'MANAGER')")
    public List<UserResponse> findAll() {
        return userMapper.toResponseList(userService.findAll());
    }

    @PostMapping("/staff")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createStaff(@Valid @RequestBody StaffCreateRequest request) {
        return userMapper.toResponse(userService.createStaff(request, request.getRole()));
    }
}
