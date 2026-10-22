package com.example.darkstore

import com.example.darkstore.dataAccess.models.RoleEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.put
import java.math.BigDecimal

class OrderFlowTest : IntegrationTestBase() {

    // 1
    @Test
    fun `guest order with card reserves stock and total is calculated by server`() {
        val productId = createProduct(price = "150.50", quantity = 10)

        val order = createOrder(token = null, items = listOf(productId to 3))

        assertEquals("CREATED", order["status"].asText())
        assertEquals(0, BigDecimal("451.50").compareTo(order["totalPrice"].decimalValue()))
        val stock = stockOf(productId)
        assertEquals(10, stock.quantity)
        assertEquals(3, stock.reservedQuantity)
    }

    // 2
    @Test
    fun `guest cannot pay with cash`() {
        val productId = createProduct()

        postOrder(token = null, items = listOf(productId to 1), paymentType = "CASH")
            .andExpect { status { isBadRequest() } }

        assertEquals(0, stockOf(productId).reservedQuantity)
    }

    // 3
    @Test
    fun `authenticated user can pay with cash only for own phone`() {
        val productId = createProduct()
        val client = createUserAndLogin(RoleEnum.CLIENT)

        // свой номер в другом формате: 8XXXXXXXXXX нормализуется в +7XXXXXXXXXX
        val ownPhoneOtherFormat = "8" + client.phone.removePrefix("+7")
        val order = createOrder(client.token, listOf(productId to 1), "CASH", ownPhoneOtherFormat)
        assertEquals(client.id.toString(), order["userId"].asText())

        postOrder(client.token, listOf(productId to 1), "CASH", randomPhone())
            .andExpect { status { isBadRequest() } }

        assertEquals(1, stockOf(productId).reservedQuantity)
    }

    // 4
    @Test
    fun `order is atomic - if one item lacks stock nothing is reserved`() {
        val enough = createProduct(quantity = 10)
        val scarce = createProduct(quantity = 1)

        postOrder(token = null, items = listOf(enough to 2, scarce to 5))
            .andExpect { status { isBadRequest() } }

        assertEquals(0, stockOf(enough).reservedQuantity)
        assertEquals(0, stockOf(scarce).reservedQuantity)
    }

    // 5
    @Test
    fun `full status lifecycle deducts stock on assembly`() {
        val productId = createProduct(quantity = 10)
        val orderId = createOrder(null, listOf(productId to 4))["id"].asText()
        val admin = adminToken()

        changeStatus(orderId, "ASSEMBLED", admin).andExpect { status { isOk() } }
        val stock = stockOf(productId)
        assertEquals(6, stock.quantity)
        assertEquals(0, stock.reservedQuantity)

        for (next in listOf("SHIPPED", "DELIVERED", "COMPLETED")) {
            changeStatus(orderId, next, admin).andExpect {
                status { isOk() }
                jsonPath("$.status") { value(next) }
            }
        }
    }

    // 6
    @Test
    fun `invalid status transition returns 409`() {
        val productId = createProduct()
        val orderId = createOrder(null, listOf(productId to 1))["id"].asText()
        val admin = adminToken()

        changeStatus(orderId, "SHIPPED", admin).andExpect { status { isConflict() } }

        mockMvc.get("/api/v1/orders/$orderId") { auth(admin) }
            .andExpect { jsonPath("$.status") { value("CREATED") } }
    }

    // 7
    @Test
    fun `roles are enforced on status changes`() {
        val productId = createProduct()
        val orderId = createOrder(null, listOf(productId to 1))["id"].asText()
        val courier = createUserAndLogin(RoleEnum.COURIER)
        val client = createUserAndLogin(RoleEnum.CLIENT)

        changeStatus(orderId, "ASSEMBLED", courier.token).andExpect { status { isForbidden() } }
        changeStatus(orderId, "PAID", client.token).andExpect { status { isForbidden() } }

        assertEquals(1, stockOf(productId).reservedQuantity)
    }

    // 8
    @Test
    fun `owner can cancel own order, other user cannot`() {
        val productId = createProduct(quantity = 5)
        val owner = createUserAndLogin(RoleEnum.CLIENT)
        val stranger = createUserAndLogin(RoleEnum.CLIENT)
        val orderId = createOrder(owner.token, listOf(productId to 2))["id"].asText()

        mockMvc.patch("/api/v1/orders/my/$orderId/cancel") { auth(stranger.token) }
            .andExpect { status { isForbidden() } }
        assertEquals(2, stockOf(productId).reservedQuantity)

        mockMvc.patch("/api/v1/orders/my/$orderId/cancel") { auth(owner.token) }
            .andExpect { status { isNoContent() } }
        assertEquals(0, stockOf(productId).reservedQuantity)

        mockMvc.get("/api/v1/orders/$orderId") { auth(owner.token) }
            .andExpect { jsonPath("$.status") { value("CANCELLED") } }
    }

    // 9
    @Test
    fun `staff can change, add and remove order items`() {
        val productA = createProduct(price = "100.00", quantity = 10)
        val productB = createProduct(price = "50.00", quantity = 10)
        val order = createOrder(null, listOf(productA to 2))
        val orderId = order["id"].asText()
        val itemAId = order["items"][0]["id"].asText()
        val admin = adminToken()

        // A: 2 -> 5, добавляем B x1
        mockMvc.put("/api/v1/orders/$orderId/items") {
            contentType = MediaType.APPLICATION_JSON
            content = json(
                mapOf(
                    "items" to listOf(
                        mapOf("orderItemId" to itemAId, "productId" to productA, "quantity" to 5),
                        mapOf("productId" to productB, "quantity" to 1),
                    )
                )
            )
            auth(admin)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalPrice") { value(550.0) }
        }
        assertEquals(5, stockOf(productA).reservedQuantity)
        assertEquals(1, stockOf(productB).reservedQuantity)

        // удаляем A, передавая только orderItemId
        mockMvc.put("/api/v1/orders/$orderId/items") {
            contentType = MediaType.APPLICATION_JSON
            content = json(mapOf("items" to listOf(mapOf("orderItemId" to itemAId, "remove" to true))))
            auth(admin)
        }.andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(1) }
            jsonPath("$.totalPrice") { value(50.0) }
        }
        assertEquals(0, stockOf(productA).reservedQuantity)
        assertEquals(1, stockOf(productB).reservedQuantity)
    }
}