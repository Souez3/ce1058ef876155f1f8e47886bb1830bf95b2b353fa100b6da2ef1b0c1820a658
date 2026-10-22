package com.example.darkstore.dataAccess.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

@Entity
@Table(name = "products")
class Product : BaseEntity() {

    @field:NotBlank
    @Column(nullable = false, unique = true)
    lateinit var name: String

    var description: String? = null

    var pictureUrl: String? = null

    @field:NotNull
    @field:Positive
    @Column(nullable = false, precision = 10, scale = 2)
    lateinit var price: BigDecimal

    @field:NotNull
    @Enumerated(EnumType.STRING)
    lateinit var category: CategoryEnum

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category")
    var subCategory: SubcategoryEnum? = null
}
