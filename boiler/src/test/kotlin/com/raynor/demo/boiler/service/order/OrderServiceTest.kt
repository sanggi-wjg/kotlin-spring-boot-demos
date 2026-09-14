package com.raynor.demo.boiler.service.order

import com.raynor.demo.boiler.domain.order.OrderItemLine
import com.raynor.demo.boiler.repository.OrderItemRepository
import com.raynor.demo.boiler.repository.OrderRepository
import com.raynor.demo.boiler.repository.ProductRepository
import com.raynor.demo.boiler.repository.UserRepository
import com.raynor.demo.boiler.service.order.model.CreateOrderRequest
import com.raynor.demo.boiler.service.order.model.OrderModel
import com.raynor.demo.boiler.service.support.CursorSlice
import com.raynor.demo.boiler.support.ServiceTestContext
import com.raynor.demo.boiler.support.fixture.OrderFixture
import com.raynor.demo.boiler.support.fixture.ProductFixture
import com.raynor.demo.boiler.support.fixture.UserFixture
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class OrderServiceTest(
    private val orderService: OrderService,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
) : ServiceTestContext({

        beforeEach {
            listOf(
                orderItemRepository,
                orderRepository,
                productRepository,
                userRepository,
            ).forEach {
                it.deleteAllInBatch()
            }
        }

        context("유저 주문 조회") {

            test("조회 성공") {
                // given
                val user = userRepository.save(UserFixture.general())
                val product = productRepository.save(ProductFixture.general())
                val order = orderRepository.save(
                    OrderFixture.pending(user, listOf(OrderItemLine(product, 1L))),
                )

                val expected = CursorSlice(
                    hasNext = false,
                    nextCursor = order.id,
                    items = listOf(order).map { OrderModel.fromEntity(it) },
                )

                // when
                val result = orderService.getUserOrders(
                    size = 10,
                    cursor = null,
                    userId = user.id!!,
                    orderStatus = listOf(order.status),
                )

                // then
                result shouldBeEqual expected
            }
        }

        context("createPendingOrder") {
            test("주문 pending 생성") {
                // given
                val user = userRepository.save(UserFixture.general())
                val product = productRepository.save(ProductFixture.general(stockQuantity = 10))

                // when
                val result = orderService.createPendingOrder(
                    userId = user.id!!,
                    request = CreateOrderRequest(
                        items = listOf(
                            CreateOrderRequest.Item(product.id!!, 1L),
                        ),
                        couponId = null,
                    ),
                )

                // then
                val findOrder = orderRepository.findTopByOrderByIdDesc()
                findOrder shouldNotBe null
                val latestOrder = findOrder!!

                result shouldBe OrderModel(id = latestOrder.id!!)
            }
        }
    })
