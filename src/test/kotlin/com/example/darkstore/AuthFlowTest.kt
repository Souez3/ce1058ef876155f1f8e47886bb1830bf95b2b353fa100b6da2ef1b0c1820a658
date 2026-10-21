package com.example.darkstore

import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class AuthFlowTest : IntegrationTestBase() {

    private fun register(phone: String = randomPhone()): JsonNode = read(
        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = json(
                mapOf("firstName" to "Иван", "phone" to phone, "password" to TEST_PASSWORD)
            )
        }.andExpect { status { isCreated() } }.andReturn()
    )

    private fun refresh(refreshToken: String) =
        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = json(mapOf("refreshToken" to refreshToken))
        }

    // 12
    @Test
    fun `register, login and access own profile`() {
        val phone = randomPhone()
        val registered = register(phone)

        mockMvc.get("/api/v1/users/me") { auth(registered["accessToken"].asText()) }
            .andExpect {
                status { isOk() }
                jsonPath("$.phone") { value(phone) }
                jsonPath("$.role") { value("CLIENT") }
            }

        val loginToken = login(phone, TEST_PASSWORD)
        mockMvc.get("/api/v1/users/me") { auth(loginToken) }
            .andExpect { status { isOk() } }
    }

    // 13
    @Test
    fun `wrong password and unknown phone give the same 401`() {
        val phone = randomPhone()
        register(phone)

        for (credentials in listOf(phone to "wrong-password", randomPhone() to TEST_PASSWORD)) {
            mockMvc.post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = json(mapOf("phone" to credentials.first, "password" to credentials.second))
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.message") { value("Invalid phone or password") }
            }
        }
    }

    // 14
    @Test
    fun `protected endpoints require token`() {
        mockMvc.get("/api/v1/users/me").andExpect { status { isUnauthorized() } }
        mockMvc.get("/api/v1/orders/my").andExpect { status { isUnauthorized() } }
        mockMvc.get("/api/v1/users/me") { auth("garbage.token.value") }
            .andExpect { status { isUnauthorized() } }
    }

    // 15
    @Test
    fun `reused refresh token revokes all user tokens`() {
        val firstRefresh = register()["refreshToken"].asText()

        val secondRefresh = read(
            refresh(firstRefresh).andExpect { status { isOk() } }.andReturn()
        )["refreshToken"].asText()

        // повторное использование старого токена: подозрение на кражу
        refresh(firstRefresh).andExpect { status { isUnauthorized() } }
        // поэтому отозван и новый
        refresh(secondRefresh).andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `refresh token does not work after logout`() {
        val refreshToken = register()["refreshToken"].asText()

        mockMvc.post("/api/v1/auth/logout") {
            contentType = MediaType.APPLICATION_JSON
            content = json(mapOf("refreshToken" to refreshToken))
        }.andExpect { status { isNoContent() } }

        refresh(refreshToken).andExpect { status { isUnauthorized() } }
    }
}