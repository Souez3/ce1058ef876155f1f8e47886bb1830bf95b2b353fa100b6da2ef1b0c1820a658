package com.example.darkstore

import com.example.darkstore.application.services.ProductService
import com.example.darkstore.application.services.StockService
import com.example.darkstore.dataAccess.models.CategoryEnum
import com.example.darkstore.dataAccess.models.Product
import com.example.darkstore.dataAccess.models.RoleEnum
import com.example.darkstore.dataAccess.models.Stock
import com.example.darkstore.dataAccess.models.User
import com.example.darkstore.dataAccess.repositories.StockRepository
import com.example.darkstore.dataAccess.repositories.UserRepository
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    @Autowired protected lateinit var mockMvc: MockMvc
    @Autowired protected lateinit var objectMapper: ObjectMapper
    @Autowired protected lateinit var productService: ProductService
    @Autowired protected lateinit var stockService: StockService
    @Autowired protected lateinit var stockRepository: StockRepository
    @Autowired protected lateinit var userRepository: UserRepository
    @Autowired protected lateinit var passwordEncoder: PasswordEncoder

    companion object {
        const val ADMIN_PHONE = "+70000000001"
        const val ADMIN_PASSWORD = "admin123"
        const val TEST_PASSWORD = "password123"

        @JvmField
        @field:ServiceConnection
        val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                start()
                Runtime.getRuntime().addShutdownHook(Thread { stop() })
            }
    }

    data class TestUser(val user: User, val token: String) {
        val id: UUID get() = requireNotNull(user.id)
        val phone: String get() = user.phone
    }

    protected fun json(body: Any): String = objectMapper.writeValueAsString(body)

    protected fun read(result: MvcResult): JsonNode =
        objectMapper.readTree(result.response.contentAsString)

    protected fun randomPhone(): String =
        "+79" + (1..9).joinToString("") { Random.nextInt(10).toString() }

    protected fun createUserAndLogin(role: RoleEnum): TestUser {
        val user = User()
        user.firstName = "Test ${role.name}"
        user.phone = randomPhone()
        user.passwordHash = passwordEncoder.encode(TEST_PASSWORD)
        user.role = role
        val saved = userRepository.save(user)
        return TestUser(saved, login(saved.phone, TEST_PASSWORD))
    }

    protected fun login(phone: String, password: String): String {
        val result = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = json(mapOf("phone" to phone, "password" to password))
        }.andExpect { status { isOk() } }.andReturn()
        return read(result)["accessToken"].asText()
    }

    protected fun adminToken(): String = login(ADMIN_PHONE, ADMIN_PASSWORD)

    protected fun MockHttpServletRequestDsl.auth(token: String?) {
        token?.let { header(HttpHeaders.AUTHORIZATION, "Bearer $it") }
    }

    protected fun createProduct(price: String = "100.00", quantity: Int = 10): UUID {
        val product = Product()
        product.name = "Test product ${UUID.randomUUID()}"
        product.price = BigDecimal(price)
        product.category = CategoryEnum.GROCERY
        val id = requireNotNull(productService.create(product).id)
        if (quantity > 0) stockService.addStock(id, quantity, null)
        return id
    }

    protected fun stockOf(productId: UUID): Stock =
        requireNotNull(stockRepository.findByProductId(productId))

    protected fun orderRequest(
        items: List<Pair<UUID, Int>>,
        paymentType: String = "CARD",
        phone: String = randomPhone(),
    ): String = json(
        mapOf(
            "customerName" to "Test Customer",
            "customerPhone" to phone,
            "paymentType" to paymentType,
            "address" to mapOf("city" to "Тестовый город", "street" to "Тестовая", "house" to "1"),
            "items" to items.map { (id, qty) -> mapOf("productId" to id, "quantity" to qty) },
        )
    )

    protected fun postOrder(
        token: String?,
        items: List<Pair<UUID, Int>>,
        paymentType: String = "CARD",
        phone: String = randomPhone(),
    ): ResultActionsDsl = mockMvc.post("/api/v1/orders") {
        contentType = MediaType.APPLICATION_JSON
        content = orderRequest(items, paymentType, phone)
        auth(token)
    }

    protected fun createOrder(
        token: String?,
        items: List<Pair<UUID, Int>>,
        paymentType: String = "CARD",
        phone: String = randomPhone(),
    ): JsonNode = read(
        postOrder(token, items, paymentType, phone)
            .andExpect { status { isCreated() } }
            .andReturn()
    )

    protected fun changeStatus(orderId: String, status: String, token: String): ResultActionsDsl =
        mockMvc.patch("/api/v1/orders/$orderId/status") {
            param("status", status)
            auth(token)
        }
}