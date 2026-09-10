package com.raynor.demo.boiler.support.fixture

import com.raynor.demo.boiler.domain.order.Order
import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.domain.support.toMoney
import com.raynor.demo.boiler.domain.user.User

object OrderFixture {
    fun pending(user: User) =
        Order(
            user = user,
            status = OrderStatus.PENDING,
            amount = 10_000.toMoney(),
            couponDiscountAmount = 0.toMoney(),
        )
}
