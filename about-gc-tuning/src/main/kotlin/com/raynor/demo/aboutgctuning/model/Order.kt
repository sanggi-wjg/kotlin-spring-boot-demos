package com.raynor.demo.aboutgctuning.model

import com.raynor.demo.aboutgctuning.entity.OrderEntity
import com.raynor.demo.aboutgctuning.enum.OrderStatus
import java.io.Serializable
import java.math.BigDecimal
import java.time.Instant

data class Order(
    val id: Int,
    val orderNumber: String,
    val amount: BigDecimal,
    val quantity: Int,
    val status: OrderStatus,
    val createdAt: Instant,
    val completedAt: Instant?,
) : Serializable {

    companion object {
        fun valueOf(orderEntity: OrderEntity): Order {
            return Order(
                id = orderEntity.id!!,
                orderNumber = orderEntity.orderNumber,
                amount = orderEntity.amount,
                quantity = orderEntity.quantity,
                status = orderEntity.status,
                createdAt = orderEntity.createdAt,
                completedAt = orderEntity.completedAt
            )
        }
    }
}
