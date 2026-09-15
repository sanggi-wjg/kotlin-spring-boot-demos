package com.raynor.demo.boiler.infra.payment

import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.infra.payment.model.PaymentResult
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

interface PaymentClient {
    fun fetchResult(orderId: Long): PaymentResult
}

@Component
class FakePaymentClient : PaymentClient {
    private val results by lazy { ConcurrentHashMap<Long, PaymentResult>() }

    fun register(result: PaymentResult) {
        results[result.orderId] = result
    }

    fun clear() {
        results.clear()
    }

    override fun fetchResult(orderId: Long): PaymentResult {
        return results[orderId] ?: PaymentResult(orderId, OrderStatus.PAID)
    }
}
