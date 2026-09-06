package com.example.darkstore.dataAccess.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "delivery_addresses")
@Getter
@Setter
public class DeliveryAddress extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String house;

    private String apartment;

    @Column(nullable = false)
    private String city;

    private String entrance;

    private String floor;
}
