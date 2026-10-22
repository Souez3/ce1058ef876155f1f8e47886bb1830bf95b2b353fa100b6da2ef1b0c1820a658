package com.example.darkstore.application.services

import com.example.darkstore.application.exceptions.EntityNotFoundException
import com.example.darkstore.dataAccess.models.Product
import com.example.darkstore.dataAccess.models.Stock
import com.example.darkstore.dataAccess.repositories.StockRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
@Transactional
class StockService(
    private val stockRepository: StockRepository,
) {
    private val log = LoggerFactory.getLogger(StockService::class.java)

    fun getStockByProductId(productId: UUID): Stock =
        stockRepository.findByProductId(productId)
            ?: throw EntityNotFoundException("Stock not found for product: $productId")

    fun isAvailable(productId: UUID, requestedQuantity: Int): Boolean =
        getStockByProductId(productId).availableQuantity >= requestedQuantity

    fun reserve(productId: UUID, quantity: Int) {
        validateQuantity(quantity)
        val stock = getStockByProductId(productId)

        require(stock.availableQuantity >= quantity) {
            "Not enough stock for product $productId. Available: ${stock.availableQuantity}, Requested: $quantity"
        }

        stock.reservedQuantity += quantity
        stockRepository.save(stock)
        log.info("Reserved {} units of product {}", quantity, productId)
    }

    fun confirmReservation(productId: UUID, quantity: Int) {
        validateQuantity(quantity)
        val stock = getStockByProductId(productId)

        check(stock.reservedQuantity >= quantity) {
            "Cannot confirm reservation for product $productId. Reserved: ${stock.reservedQuantity}, Requested: $quantity"
        }

        stock.quantity -= quantity
        stock.reservedQuantity -= quantity
        stockRepository.save(stock)
        log.info("Confirmed reservation for {} units of product {}", quantity, productId)
    }

    fun cancelReservation(productId: UUID, quantity: Int) {
        validateQuantity(quantity)
        val stock = getStockByProductId(productId)

        check(stock.reservedQuantity >= quantity) {
            "Cannot cancel reservation for product $productId. Reserved: ${stock.reservedQuantity}, Requested: $quantity"
        }

        stock.reservedQuantity -= quantity
        stockRepository.save(stock)
        log.info("Cancelled reservation for {} units of product {}", quantity, productId)
    }

    fun addStock(productId: UUID, quantity: Int, purchasePrice: BigDecimal?) {
        validateQuantity(quantity)
        val stock = getStockByProductId(productId)

        stock.quantity += quantity
        purchasePrice?.let { stock.purchasePrice = it }
        stockRepository.save(stock)
        log.info("Added {} units to product {} at price {}", quantity, productId, purchasePrice)
    }

    fun initializeStockForProduct(product: Product) {
        val stock = Stock()
        stock.product = product
        stock.quantity = 0
        stock.reservedQuantity = 0
        stock.purchasePrice = BigDecimal.ZERO
        stockRepository.save(stock)
    }

    private fun validateQuantity(quantity: Int) {
        require(quantity > 0) { "Quantity must be positive" }
    }
}
