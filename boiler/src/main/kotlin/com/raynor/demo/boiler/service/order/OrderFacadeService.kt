package com.raynor.demo.boiler.service.order

import com.raynor.demo.boiler.infra.redis.lock.DistributedLockExecutor
import com.raynor.demo.boiler.service.order.model.CreateOrderRequest
import com.raynor.demo.boiler.service.order.model.OrderModel
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class OrderFacadeService(
    private val orderService: OrderService,
    private val lockExecutor: DistributedLockExecutor,
) {
    @Retryable(
        includes = [ObjectOptimisticLockingFailureException::class],
        maxRetries = 3,
        delay = 100L,
    )
    fun createOrder(
        userId: Int,
        request: CreateOrderRequest,
    ): OrderModel {
        val lockKeys = request.items.map { item -> "product:${item.productId}" }
        val order = lockExecutor.execute(lockKeys, 10L, action = {
            orderService.createPendingOrder(userId, request)
        })

        return order
    }
}
