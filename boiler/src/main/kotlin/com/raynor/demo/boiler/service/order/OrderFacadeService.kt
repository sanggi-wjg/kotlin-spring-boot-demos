package com.raynor.demo.boiler.service.order

import com.raynor.demo.boiler.infra.payment.PaymentClient
import com.raynor.demo.boiler.infra.redis.lock.DistributedLockExecutor
import com.raynor.demo.boiler.service.order.model.CreateOrderRequest
import com.raynor.demo.boiler.service.order.model.OrderModel
import com.raynor.demo.boiler.service.order.model.OrderWebhookRequest
import com.raynor.demo.boiler.service.order.model.WebhookResult
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class OrderFacadeService(
    private val lockExecutor: DistributedLockExecutor,
    private val paymentClient: PaymentClient,
    private val orderService: OrderService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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

    @Retryable(
        includes = [ObjectOptimisticLockingFailureException::class],
        maxRetries = 3,
        delay = 100L,
    )
    fun handleOrderWebhook(request: OrderWebhookRequest): WebhookResult {
        // 웹훅 요청, 응답 등은 저장하며 독립 트랜잭션으로 실행 (구현 했다고 하고 스킵)
        val paymentResult = paymentClient.fetchResult(request.orderId)
        // 금액 등 필요한 내용 검증 가정 (구현 했다고 하고 스킵)
        if (paymentResult.status != request.status) {
            val message = "payment status mismatch. webhook=${request.status}, pg=${paymentResult.status}"
            logger.warn("Webhook failed. orderId={}, reason={}", request.orderId, message)
            return WebhookResult(request.orderId, null, WebhookResult.Result.FAILED, message)
        }

        val result = try {
            orderService.handleOrderWebhook(paymentResult)
        } catch (e: EntityNotFoundException) {
            logger.warn("Webhook failed. orderId={}, reason={}", request.orderId, e.message)
            WebhookResult(request.orderId, null, WebhookResult.Result.FAILED, e.message)
        } catch (e: IllegalStateException) {
            logger.warn("Webhook failed. orderId={}, reason={}", request.orderId, e.message)
            WebhookResult(request.orderId, null, WebhookResult.Result.FAILED, e.message)
        }
        // 이후 알림 큐나 이벤트 발행 등 구현 (구현 했다고 하고 스킵)
        return result
    }
}
