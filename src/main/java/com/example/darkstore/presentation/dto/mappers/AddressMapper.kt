package com.example.darkstore.presentation.dto.mappers

import com.example.darkstore.dataAccess.models.DeliveryAddress
import com.example.darkstore.presentation.dto.objects.order.DeliveryAddressDto

fun DeliveryAddressDto.toEntity(): DeliveryAddress {
    val address = DeliveryAddress()
    address.city = requireNotNull(city) { "city is required" }
    address.street = requireNotNull(street) { "street is required" }
    address.house = requireNotNull(house) { "house is required" }
    address.entrance = entrance
    address.floor = floor
    address.apartment = apartment
    // user = null in guest order case
    // no intercom
    return address
}

fun DeliveryAddress.toDto(): DeliveryAddressDto = DeliveryAddressDto(
    city = city,
    street = street,
    house = house,
    entrance = entrance,
    floor = floor,
    apartment = apartment,
    intercom = null,
)
