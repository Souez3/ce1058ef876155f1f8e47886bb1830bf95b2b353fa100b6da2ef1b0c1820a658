package com.example.darkstore.presentation.dto.objects.order;

import com.example.darkstore.dataAccess.models.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryAddressDto {
    @NotBlank
    private String city;

    @NotBlank
    private String street;

    @NotBlank
    private String house;

    private String entrance;

    private String floor;

    private String apartment;

    private String intercom;
}
