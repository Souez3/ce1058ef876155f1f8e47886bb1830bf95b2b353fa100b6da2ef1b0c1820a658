package com.example.darkstore.presentation.dto.objects.user;

import com.example.darkstore.dataAccess.models.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private RoleEnum role;
    private Instant createdAt;
}
