package com.raynor.demo.boiler.service.order

import com.raynor.demo.boiler.domain.order.Order
import com.raynor.demo.boiler.domain.order.OrderItemLine
import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.domain.product.ProductStatus
import com.raynor.demo.boiler.infra.payment.model.PaymentResult
import com.raynor.demo.boiler.repository.OrderRepository
import com.raynor.demo.boiler.repository.ProductRepository
import com.raynor.demo.boiler.repository.UserRepository
import com.raynor.demo.boiler.service.order.model.CreateOrderRequest
import com.raynor.demo.boiler.service.order.model.OrderModel
import com.raynor.demo.boiler.service.order.model.WebhookResult
import com.raynor.demo.boiler.service.support.CursorSlice
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
) {
    private val log = LoggerFactory.getLogger(OrderService::class.java)

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

    @Transactional
    fun createPendingOrder(
        userId: Int,
        request: CreateOrderRequest,
    ): OrderModel {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw EntityNotFoundException("User not found")
        val requestItemMap = request.items.associateBy { it.productId }
        val products = productRepository.findAllByIdInAndDeletedAtIsNullAndStatus(
            ids = request.items.map { it.productId },
            status = ProductStatus.ON_SALE,
        )

        val orderItemLines = products.map { product ->
            val requestItem = requestItemMap[product.id]
                ?: throw IllegalArgumentException("Invalid product id")
            OrderItemLine(product, requestItem.quantity)
        }
        orderItemLines.forEach { orderItemLine ->
            orderItemLine.product.decreaseStock(orderItemLine.quantity)
        }

        val order = Order.pending(user, orderItemLines)
        return orderRepository.save(order).let { order -> OrderModel.fromEntity(order) }
    }

    @Transactional
    fun handleOrderWebhook(paymentResult: PaymentResult): WebhookResult {
        val order = orderRepository.findByIdWithLock(paymentResult.orderId)
            ?: throw EntityNotFoundException("Order not found. orderId=${paymentResult.orderId}")
        val orderId = order.id!!

        if (order.status == paymentResult.status) {
            log.info("Order:{} is already {}", orderId, order.status)
            return WebhookResult(orderId, order.status, WebhookResult.Result.ALREADY_PROCESSED)
        }

        order.transitionTo(paymentResult.status)
        return WebhookResult(orderId, order.status, WebhookResult.Result.PROCESSED)
    }
}
