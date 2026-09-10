package com.raynor.demo.boiler.service.order

import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.repository.OrderRepository
import com.raynor.demo.boiler.service.order.model.OrderModel
import com.raynor.demo.boiler.service.support.CursorSlice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
) {
    @Transactional(readOnly = true)
    fun getUserOrders(
        size: Int,
        cursor: Long?,
        userId: Int,
        orderStatus: List<OrderStatus>?,
    ): CursorSlice<Long, OrderModel> {
        val orders = orderRepository.findAllByUserAndCursor(
            size = size.toLong() + 1,
            cursorId = cursor,
            userId = userId,
            orderStatus = orderStatus,
        )
        val items = orders.take(size).map { OrderModel.fromEntity(it) }
        return CursorSlice(
            hasNext = orders.size > size,
            nextCursor = items.lastOrNull()?.id,
            items = items,
        )
    }
}
