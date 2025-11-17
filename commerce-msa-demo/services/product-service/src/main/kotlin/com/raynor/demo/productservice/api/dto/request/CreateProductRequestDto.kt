package com.raynor.demo.productservice.api.dto.request

import com.raynor.demo.productservice.service.model.command.CreateProductCommand
import com.raynor.demo.shared.typed.product.toProductName
import com.raynor.demo.shared.typed.product.toProductStockQuantity
import com.raynor.demo.shared.typed.toMoney
import jakarta.validation.constraints.*
import java.math.BigDecimal

data class CreateProductRequestDto(
    @field:NotBlank(message = "name is required")
    @field:Size(max = 100, message = "name must be less than 100 characters")
    val name: String,

    @field:NotNull(message = "price is required")
    @field:DecimalMin(value = "0.0", message = "price must be greater than or equal to 0")
    @field:Digits(integer = 10, fraction = 2, message = "price must have at most 10 integer digits and 2 decimal places")
    val price: BigDecimal,

    @field:NotNull(message = "stockQuantity is required")
    @field:PositiveOrZero(message = "stockQuantity must be greater than or equal to 0")
    val stockQuantity: Int
) {
    fun toCommand() = CreateProductCommand(
        name = name.toProductName(),
        price = price.toMoney(),
        stockQuantity = stockQuantity.toProductStockQuantity(),
    )
}
