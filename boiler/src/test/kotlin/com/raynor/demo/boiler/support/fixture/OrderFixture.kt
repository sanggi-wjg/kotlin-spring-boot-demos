package com.raynor.demo.boiler.support.fixture

import com.raynor.demo.boiler.domain.order.Order
import com.raynor.demo.boiler.domain.order.OrderItemLine
import com.raynor.demo.boiler.domain.user.User

object OrderFixture {
    fun pending(
        user: User,
        orderItemLines: List<OrderItemLine>,
    ): Order {
        return Order.pending(user, orderItemLines)
    }
}
