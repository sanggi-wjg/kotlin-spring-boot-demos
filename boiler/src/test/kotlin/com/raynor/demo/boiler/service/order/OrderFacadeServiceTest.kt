package com.raynor.demo.boiler.service.order

import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.infra.payment.FakePaymentClient
import com.raynor.demo.boiler.infra.payment.model.PaymentResult
import com.raynor.demo.boiler.repository.OrderItemRepository
import com.raynor.demo.boiler.repository.OrderRepository
import com.raynor.demo.boiler.repository.ProductRepository
import com.raynor.demo.boiler.repository.UserRepository
import com.raynor.demo.boiler.service.order.model.CreateOrderRequest
import com.raynor.demo.boiler.service.order.model.OrderModel
import com.raynor.demo.boiler.service.order.model.OrderWebhookRequest
import com.raynor.demo.boiler.service.order.model.WebhookResult
import com.raynor.demo.boiler.support.ServiceTestContext
import com.raynor.demo.boiler.support.fixture.ProductFixture
import com.raynor.demo.boiler.support.fixture.UserFixture
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.springframework.data.repository.findByIdOrNull

class OrderFacadeServiceTest(
    private val orderFacadeService: OrderFacadeService,
    private val orderService: OrderService,
    private val fakePaymentClient: FakePaymentClient,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
) : ServiceTestContext(
        {

            beforeEach {
                fakePaymentClient.clear()
                listOf(
                    orderItemRepository,
                    orderRepository,
                    productRepository,
                    userRepository,
                ).forEach {
                    it.deleteAllInBatch()
                }
            }

            afterTest {
                fakePaymentClient.clear()
                listOf(
                    orderItemRepository,
                    orderRepository,
                    productRepository,
                    userRepository,
                ).forEach {
                    it.deleteAllInBatch()
                }
            }

            test("주문 생성") {
                // given
                val userId = 1
                val request = CreateOrderRequest(
                    items = listOf(CreateOrderRequest.Item(10, 1)),
                    couponId = null,
                )

                val orderModel = OrderModel(id = 999)

                // mock
                every { orderService.createPendingOrder(userId, request) } returns orderModel

                // when
                val result = orderFacadeService.createOrder(userId, request)

                // then
                result shouldBe orderModel
            }

            context("결제 웹훅") {

                fun pendingOrder(
                    stock: Long = 5L,
                    quantity: Long = 2L,
                ): Pair<OrderModel, Int> {
                    val user = userRepository.save(UserFixture.general())
                    val product = productRepository.save(ProductFixture.general(stockQuantity = stock))
                    val order = orderService.createPendingOrder(
                        userId = user.id!!,
                        request = CreateOrderRequest(
                            items = listOf(CreateOrderRequest.Item(product.id!!, quantity)),
                            couponId = null,
                        ),
                    )
                    return order to product.id!!
                }

                test("결제 완료 웹훅은 주문을 PAID 로 바꾼다") {
                    // given
                    val (order, _) = pendingOrder()
                    fakePaymentClient.register(PaymentResult(order.id, OrderStatus.PAID))

                    // when
                    val result = orderFacadeService.handleOrderWebhook(OrderWebhookRequest(order.id, OrderStatus.PAID))

                    // then
                    result.result shouldBe WebhookResult.Result.PROCESSED
                    result.orderStatus shouldBe OrderStatus.PAID
                    orderRepository.findByIdOrNull(order.id).shouldNotBeNull().status shouldBe OrderStatus.PAID
                }

                test("결제 실패 웹훅은 주문을 PAYMENT_FAILED 로 바꾸고 재고를 복구한다") {
                    // given
                    val (order, productId) = pendingOrder(stock = 5L, quantity = 2L)
                    productRepository.findByIdOrNull(productId).shouldNotBeNull().stock.quantity shouldBe 3L
                    fakePaymentClient.register(PaymentResult(order.id, OrderStatus.PAYMENT_FAILED))

                    // when
                    val result = orderFacadeService.handleOrderWebhook(OrderWebhookRequest(order.id, OrderStatus.PAYMENT_FAILED))

                    // then
                    result.result shouldBe WebhookResult.Result.PROCESSED
                    result.orderStatus shouldBe OrderStatus.PAYMENT_FAILED
                    orderRepository.findByIdOrNull(order.id).shouldNotBeNull().status shouldBe OrderStatus.PAYMENT_FAILED
                    productRepository.findByIdOrNull(productId).shouldNotBeNull().stock.quantity shouldBe 5L
                }

                test("같은 웹훅이 다시 오면 ALREADY_PROCESSED 를 돌려주고 상태는 그대로다") {
                    // given
                    val (order, productId) = pendingOrder(stock = 5L, quantity = 2L)
                    fakePaymentClient.register(PaymentResult(order.id, OrderStatus.PAYMENT_FAILED))
                    val request = OrderWebhookRequest(order.id, OrderStatus.PAYMENT_FAILED)
                    orderFacadeService.handleOrderWebhook(request)

                    // when
                    val result = orderFacadeService.handleOrderWebhook(request)

                    // then
                    result.result shouldBe WebhookResult.Result.ALREADY_PROCESSED
                    result.orderStatus shouldBe OrderStatus.PAYMENT_FAILED
                    // 재고가 두 번 복구되지 않는다
                    productRepository.findByIdOrNull(productId).shouldNotBeNull().stock.quantity shouldBe 5L
                }

                test("PG 조회 결과와 웹훅 상태가 다르면 FAILED 를 돌려주고 주문은 바뀌지 않는다") {
                    // given
                    val (order, _) = pendingOrder()
                    fakePaymentClient.register(PaymentResult(order.id, OrderStatus.PAID))

                    // when
                    val result = orderFacadeService.handleOrderWebhook(OrderWebhookRequest(order.id, OrderStatus.PAYMENT_FAILED))

                    // then
                    result.result shouldBe WebhookResult.Result.FAILED
                    result.orderStatus shouldBe null
                    orderRepository.findByIdOrNull(order.id).shouldNotBeNull().status shouldBe OrderStatus.PENDING
                }

                test("이미 PAID 인 주문에 PAYMENT_FAILED 웹훅이 오면 FAILED 를 돌려주고 주문은 바뀌지 않는다") {
                    // given
                    val (order, _) = pendingOrder()
                    fakePaymentClient.register(PaymentResult(order.id, OrderStatus.PAID))
                    orderFacadeService.handleOrderWebhook(OrderWebhookRequest(order.id, OrderStatus.PAID))
                    fakePaymentClient.register(PaymentResult(order.id, OrderStatus.PAYMENT_FAILED))

                    // when
                    val result = orderFacadeService.handleOrderWebhook(OrderWebhookRequest(order.id, OrderStatus.PAYMENT_FAILED))

                    // then
                    result.result shouldBe WebhookResult.Result.FAILED
                    orderRepository.findByIdOrNull(order.id).shouldNotBeNull().status shouldBe OrderStatus.PAID
                }

                test("존재하지 않는 주문이면 FAILED 를 돌려준다") {
                    // when
                    val result = orderFacadeService.handleOrderWebhook(OrderWebhookRequest(999_999L, OrderStatus.PAID))

                    // then
                    result.result shouldBe WebhookResult.Result.FAILED
                    result.orderStatus shouldBe null
                }
            }
        },
    )
