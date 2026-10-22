package com.example.darkstore.dataAccess.models

import java.math.BigDecimal

class CartItem(
    var product: Product,
    var quantity: Int,
    var price: BigDecimal
) {
    val totalPrice: BigDecimal
        get() = price.multiply(BigDecimal.valueOf(quantity.toLong()))
}
