package com.example.darkstore

import com.example.darkstore.dataAccess.models.CategoryEnum
import com.example.darkstore.dataAccess.models.Product
import com.example.darkstore.dataAccess.models.RoleEnum
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.util.UUID

class ProductApiTest : IntegrationTestBase() {

    private fun createNamed(name: String, price: String, category: CategoryEnum): UUID {
        val product = Product()
        product.name = name
        product.price = BigDecimal(price)
        product.category = category
        return requireNotNull(productService.create(product).id)
    }

    // 16
    @Test
    fun `search filters by text, price and category and hides removed products`() {
        // уникальная метка, чтобы в выдачу не попали демо-данные и товары других тестов
        val tag = "srch${UUID.randomUUID().toString().take(8)}"
        createNamed("$tag cheap", "50.00", CategoryEnum.GROCERY)
        val middle = createNamed("$tag middle", "150.00", CategoryEnum.DRINKS)
        createNamed("$tag expensive", "300.00", CategoryEnum.GROCERY)
        val removed = createNamed("$tag removed", "150.00", CategoryEnum.GROCERY)

        mockMvc.delete("/api/v1/products/$removed") { auth(adminToken()) }
            .andExpect { status { isNoContent() } }

        mockMvc.get("/api/v1/products") {
            param("search", tag)
            param("minPrice", "100")
            param("maxPrice", "200")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements") { value(1) }
            jsonPath("$.content[0].id") { value(middle.toString()) }
        }

        mockMvc.get("/api/v1/products") {
            param("search", tag)
            param("category", "DRINKS")
        }.andExpect {
            jsonPath("$.totalElements") { value(1) }
            jsonPath("$.content[0].id") { value(middle.toString()) }
        }

        mockMvc.get("/api/v1/products/$removed").andExpect { status { isNotFound() } }
    }

    // 17
    @Test
    fun `only manager can create product and input is validated`() {
        val manager = createUserAndLogin(RoleEnum.MANAGER)
        val client = createUserAndLogin(RoleEnum.CLIENT)
        val body = json(
            mapOf("name" to "New ${UUID.randomUUID()}", "price" to 199.90, "category" to "SNACKS")
        )

        mockMvc.post("/api/v1/products") {
            contentType = MediaType.APPLICATION_JSON
            content = body
            auth(client.token)
        }.andExpect { status { isForbidden() } }

        mockMvc.post("/api/v1/products") {
            contentType = MediaType.APPLICATION_JSON
            content = body
            auth(manager.token)
        }.andExpect { status { isCreated() } }

        mockMvc.post("/api/v1/products") {
            contentType = MediaType.APPLICATION_JSON
            content = json(mapOf("price" to -5, "category" to "SNACKS"))
            auth(manager.token)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.fieldErrors.name") { exists() }
            jsonPath("$.fieldErrors.price") { exists() }
        }
    }

    // 18
    @Test
    fun `subcategory must belong to category`() {
        mockMvc.post("/api/v1/products") {
            contentType = MediaType.APPLICATION_JSON
            content = json(
                mapOf(
                    "name" to "Wrong ${UUID.randomUUID()}",
                    "price" to 100,
                    "category" to "DRINKS",
                    "subCategory" to "MILK",
                )
            )
            auth(adminToken())
        }.andExpect { status { isBadRequest() } }

        mockMvc.get("/api/v1/products") {
            param("category", "DRINKS")
            param("subcategory", "MILK")
        }.andExpect { status { isBadRequest() } }
    }
}