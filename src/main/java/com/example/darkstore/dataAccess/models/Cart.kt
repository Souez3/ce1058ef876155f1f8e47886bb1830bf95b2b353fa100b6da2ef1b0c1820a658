package com.example.darkstore.dataAccess.models

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext
import java.math.BigDecimal
import java.util.UUID

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class Cart {

    private val itemsByProduct: MutableMap<UUID, CartItem> = HashMap() // key = productId

    fun addItem(product: Product, quantity: Int) {
        val productId = requireNotNull(product.id) { "Product must be persisted" }
        val existing = itemsByProduct[productId]
        if (existing != null) {
            existing.quantity += quantity
        } else {
            itemsByProduct[productId] = CartItem(product, quantity, product.price)
        }
    }

    fun removeItem(productId: UUID) {
        itemsByProduct.remove(productId)
    }

    val items: List<CartItem>
        get() = itemsByProduct.values.toList()

    val totalPrice: BigDecimal
        get() = itemsByProduct.values.fold(BigDecimal.ZERO) { acc, item -> acc + item.totalPrice }

    fun clear() {
        itemsByProduct.clear()
    }
}
