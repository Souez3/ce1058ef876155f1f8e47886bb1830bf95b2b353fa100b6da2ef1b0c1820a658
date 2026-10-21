package com.example.darkstore

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class StockServiceTest : IntegrationTestBase() {

    // 10
    @Test
    fun `cannot reserve more than available`() {
        val productId = createProduct(quantity = 5)

        assertThrows<IllegalArgumentException> { stockService.reserve(productId, 6) }

        assertEquals(0, stockOf(productId).reservedQuantity)
    }

    @Test
    fun `quantity must be positive`() {
        val productId = createProduct(quantity = 5)

        assertThrows<IllegalArgumentException> { stockService.reserve(productId, 0) }
        assertThrows<IllegalArgumentException> { stockService.addStock(productId, -1, null) }
    }

    // 11
    @Test
    fun `concurrent reservations of the last unit - exactly one succeeds`() {
        val productId = createProduct(quantity = 1)
        val threads = 5
        val startSignal = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(threads)

        // Все потоки стартуют одновременно. Победит один: остальные получат
        // либо конфликт версии (@Version), либо "Not enough stock"
        val results = (1..threads).map {
            pool.submit(Callable {
                startSignal.await()
                try {
                    stockService.reserve(productId, 1)
                    true
                } catch (e: Exception) {
                    false
                }
            })
        }
        startSignal.countDown()

        val successes = results.count { it.get(10, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(1, successes)
        assertEquals(1, stockOf(productId).reservedQuantity)
    }
}