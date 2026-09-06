package com.example.darkstore.presentation.dto.mappers;

import com.example.darkstore.dataAccess.models.DeliveryAddress;
import com.example.darkstore.presentation.dto.objects.order.DeliveryAddressDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "user", ignore = true) // Пока null (гость)
    DeliveryAddress toEntity(DeliveryAddressDto dto);

    DeliveryAddressDto toDto(DeliveryAddress address);
}
