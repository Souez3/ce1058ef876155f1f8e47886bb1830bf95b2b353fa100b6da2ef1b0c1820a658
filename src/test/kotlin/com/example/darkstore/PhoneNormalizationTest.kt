package com.example.darkstore

import com.example.darkstore.application.services.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PhoneNormalizationTest {

    @ParameterizedTest
    @CsvSource(
        "'8 (912) 345-67-89', '+79123456789'",
        "'+7 912 345 67 89',  '+79123456789'",
        "'79123456789',       '+79123456789'",
        "'+79123456789',      '+79123456789'",
    )
    fun `phone is normalized to +7 format`(raw: String, expected: String) {
        assertEquals(expected, UserService.normalizePhone(raw))
    }
}