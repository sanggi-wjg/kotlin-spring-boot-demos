package com.raynor.demo.boiler.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.raynor.demo.boiler.domain.order.Order
import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.domain.order.QOrder
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository :
    JpaRepository<Order, Long>,
    OrderQueryDslRepository {
    fun findTopByOrderByIdDesc(): Order?
}

interface OrderQueryDslRepository {
    fun findAllByUserAndCursor(
        size: Long,
        cursorId: Long?,
        userId: Int,
        orderStatus: List<OrderStatus>?,
    ): List<Order>
}

class OrderQueryDslRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : OrderQueryDslRepository {
    private val order = QOrder.order

    override fun findAllByUserAndCursor(
        size: Long,
        cursorId: Long?,
        userId: Int,
        orderStatus: List<OrderStatus>?,
    ): List<Order> {
        return jpaQueryFactory.select(order)
            .from(order)
            .where(
                order.user.id.eq(userId),
                cursorId?.let { order.id.lt(it) },
                orderStatus?.let { order.status.`in`(it) },
            )
            .orderBy(order.id.desc())
            .limit(size)
            .fetch()
    }
}
