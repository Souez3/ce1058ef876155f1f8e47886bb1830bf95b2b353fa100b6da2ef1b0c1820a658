package com.example.darkstore.dataAccess.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "stock")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Stock extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false)
    private Integer reservedQuantity = 0;

    @Column(nullable = false)
    private BigDecimal purchasePrice;

    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    @Version
    private Long version;
}
