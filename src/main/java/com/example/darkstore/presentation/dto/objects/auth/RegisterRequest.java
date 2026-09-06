package com.example.darkstore.presentation.dto.objects.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Invalid phone format")
    private String phone;

    @jakarta.validation.constraints.Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    private String password;

}
