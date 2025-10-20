package com.raynor.demo.orderservice.api.dto.request

import com.raynor.demo.orderservice.service.model.command.CreateOrderCommand
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreateOrderRequestDto(
    @field:NotNull(message = "Product ID is required")
    val productId: Long,

    @field:Min(value = 1, message = "Minimum quantity is 1")
    val quantity: Int,

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.0", message = "price must be greater than or equal to 0")
    val amount: BigDecimal
) {
    fun toCommand() = CreateOrderCommand(
        productId = productId,
        quantity = quantity,
        amount = amount
    )
}
