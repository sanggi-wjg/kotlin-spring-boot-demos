package com.raynor.demo.boiler.service.order

import com.raynor.demo.boiler.domain.order.OrderStatus
import com.raynor.demo.boiler.repository.OrderRepository
import com.raynor.demo.boiler.repository.UserRepository
import com.raynor.demo.boiler.service.order.model.OrderModel
import com.raynor.demo.boiler.service.support.CursorSlice
import com.raynor.demo.boiler.support.ServiceTestContext
import com.raynor.demo.boiler.support.fixture.OrderFixture
import com.raynor.demo.boiler.support.fixture.UserFixture
import io.kotest.matchers.equals.shouldBeEqual

class OrderServiceTest(
    private val orderService: OrderService,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
) : ServiceTestContext({

        beforeEach {
            listOf(
                orderRepository,
                userRepository,
            ).forEach {
                it.deleteAllInBatch()
            }
        }

        context("유저 주문 조회") {

            test("조회 성공") {
                // given
                val user = userRepository.save(UserFixture.general())
                val order = orderRepository.save(OrderFixture.pending(user))

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
    })
