package com.raynor.demo.dbvendor.mongodb

import com.raynor.demo.dbvendor.enum.OrderStatus
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "orders")
data class OrderDocument(
    @Id
    val id: ObjectId? = null,
    val orderNumber: String,
    val amount: BigDecimal,
    val status: OrderStatus = OrderStatus.NEW_ORDER,
    val createdAt: Instant,
    val completedAt: Instant? = null,
) {
    fun complete(time: Instant): OrderDocument {
        return this.copy(
            status = OrderStatus.COMPLETED,
            completedAt = time
        )
    }
}
