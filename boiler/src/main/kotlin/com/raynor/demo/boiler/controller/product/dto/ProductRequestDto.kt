package com.raynor.demo.boiler.controller.product.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class ProductRequestDto(
    @NotBlank
    val name: String,
    val price: BigDecimal,
    @Min(0)
    val quantity: Long,
)
