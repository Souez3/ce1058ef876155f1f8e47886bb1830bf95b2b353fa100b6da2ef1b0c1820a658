package com.example.darkstore.application.services;

import com.example.darkstore.application.exceptions.EntityNotFoundException;
import com.example.darkstore.dataAccess.models.Product;
import com.example.darkstore.dataAccess.models.Stock;
import com.example.darkstore.dataAccess.repositories.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class StockService {
    private final StockRepository stockRepository;

    public Stock getStockByProductId(UUID productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found for product: " + productId));
    }

    public boolean isAvailable(UUID productId, Integer requestedQuantity) {
        Stock stock = getStockByProductId(productId);
        return stock.getAvailableQuantity() >= requestedQuantity;
    }

    @Transactional
    public void reserve(UUID productId, Integer quantity) {
        validateQuantity(quantity);

        Stock stock = getStockByProductId(productId);

        if (stock.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException(
                    String.format("Not enough stock for product %s. Available: %d, Requested: %d",
                            productId, stock.getAvailableQuantity(), quantity)
            );
        }

        stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
        stockRepository.save(stock);
        log.info("Reserved {} units of product {}", quantity, productId);
    }

    // списать со склада
    @Transactional
    public void confirmReservation(UUID productId, Integer quantity) {
        validateQuantity(quantity);

        Stock stock = getStockByProductId(productId);

        if (stock.getReservedQuantity() < quantity) {
            throw new IllegalStateException(
                    String.format("Cannot confirm reservation for product %s. Reserved: %d, Requested: %d",
                            productId, stock.getReservedQuantity(), quantity)
            );
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        stock.setReservedQuantity(stock.getReservedQuantity() - quantity);
        stockRepository.save(stock);
        log.info("Confirmed reservation for {} units of product {}", quantity, productId);
    }

    @Transactional
    public void cancelReservation(UUID productId, Integer quantity) {
        validateQuantity(quantity);

        Stock stock = getStockByProductId(productId);

        if (stock.getReservedQuantity() < quantity) {
            throw new IllegalStateException(
                    String.format("Cannot cancel reservation for product %s. Reserved: %d, Requested: %d",
                            productId, stock.getReservedQuantity(), quantity)
            );
        }

        stock.setReservedQuantity(stock.getReservedQuantity() - quantity);
        stockRepository.save(stock);
        log.info("Cancelled reservation for {} units of product {}", quantity, productId);
    }

    @Transactional
    public void addStock(UUID productId, Integer quantity, BigDecimal purchasePrice) {
        validateQuantity(quantity);

        Stock stock = getStockByProductId(productId);
        stock.setQuantity(stock.getQuantity() + quantity);
        if (purchasePrice != null) {
            stock.setPurchasePrice(purchasePrice);
        }
        stockRepository.save(stock);
        log.info("Added {} units to product {} at price {}", quantity, productId, purchasePrice);
    }

    public void initializeStockForProduct(Product product) {
        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setQuantity(0);
        stock.setReservedQuantity(0);
        stock.setPurchasePrice(BigDecimal.ZERO);

        stockRepository.save(stock);
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive"
            );
        }
    }
}
