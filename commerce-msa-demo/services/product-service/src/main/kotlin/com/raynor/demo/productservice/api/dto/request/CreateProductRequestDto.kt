package com.raynor.demo.productservice.api.dto.request

import com.raynor.demo.productservice.service.model.command.CreateProductCommand
import com.raynor.demo.shared.typed.product.toProductName
import com.raynor.demo.shared.typed.product.toProductStockQuantity
import com.raynor.demo.shared.typed.toMoney
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreateProductRequestDto(
    @field:NotBlank(message = "name is required")
    @field:Size(max = 100, message = "name must be less than 100 characters")
    val name: String,

    @field:PositiveOrZero(message = "price must be greater than or equal to 0")
    val price: BigDecimal,

    @field:PositiveOrZero(message = "stockQuantity must be greater than or equal to 0")
    val stockQuantity: Int
) {
    fun toCommand() = CreateProductCommand(
        name = name.toProductName(),
        price = price.toMoney(),
        stockQuantity = stockQuantity.toProductStockQuantity(),
    )
}
