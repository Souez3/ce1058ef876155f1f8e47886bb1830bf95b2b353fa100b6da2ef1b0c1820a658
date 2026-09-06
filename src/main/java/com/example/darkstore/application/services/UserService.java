package com.example.darkstore.application.services;

import com.example.darkstore.application.exceptions.EntityNotFoundException;
import com.example.darkstore.dataAccess.models.RoleEnum;
import com.example.darkstore.dataAccess.models.User;
import com.example.darkstore.dataAccess.repositories.UserRepository;
import com.example.darkstore.presentation.dto.objects.auth.RegisterRequest;
import com.example.darkstore.presentation.dto.objects.user.UserUpdateRequest;
import com.example.darkstore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** "8 (912) 345-67-89" -> "+79123456789" */
    public static String normalizePhone(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^\\d+]", "");
        if (digits.startsWith("8") && digits.length() == 11) {
            digits = "+7" + digits.substring(1);
        } else if (!digits.startsWith("+")) {
            digits = "+" + digits;
        }
        return digits;
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    public User getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Not authenticated"));
        return findById(userId);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User registerClient(RegisterRequest request) {
        String phone = normalizePhone(request.getPhone());

        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("User with this phone already exists");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(phone);
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleEnum.CLIENT); // роль НИКОГДА не берём из запроса

        return userRepository.save(user);
    }

    /**
     * Создание служебного пользователя (курьер, кладовщик, менеджер).
     * Дергается только из админского контроллера под SYSTEM_ADMIN/MANAGER.
     */
    @Transactional
    public User createStaff(RegisterRequest request, RoleEnum role) {
        if (role == RoleEnum.CLIENT) {
            throw new IllegalArgumentException("Use register endpoint for clients");
        }
        User user = registerClient(request);
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User updateCurrentUser(UserUpdateRequest request) {
        User user = getCurrentUser();

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}