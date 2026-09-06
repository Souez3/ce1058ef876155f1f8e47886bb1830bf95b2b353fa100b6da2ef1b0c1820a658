package com.example.darkstore.presentation.dto.objects.user;

import com.example.darkstore.dataAccess.models.RoleEnum;
import com.example.darkstore.presentation.dto.objects.auth.RegisterRequest;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffCreateRequest extends RegisterRequest {
    @NotNull
    private RoleEnum role;
}
