package com.raynor.demo.miniioc.context

import com.raynor.demo.miniioc.example.DefaultOrderRepository
import com.raynor.demo.miniioc.example.DefaultOrderService
import com.raynor.demo.miniioc.example.OrderRepository
import com.raynor.demo.miniioc.example.OrderService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class GenericApplicationContextTest :
    FunSpec(
        {

            test("refresh 전에는 빈 생성 안되어야 한다.") {
                // given
                val context = GenericApplicationContext(
                    DefaultOrderService::class,
                    DefaultOrderRepository::class,
                )

                // when & then
                context.beanFactory.containsSingleton("DefaultOrderService") shouldBe false
                context.beanFactory.containsSingleton("DefaultOrderRepository") shouldBe false
            }

            test("refresh 후에는 빈 생성이 되어있어야 한다.") {
                // given
                val context = GenericApplicationContext(
                    DefaultOrderService::class,
                    DefaultOrderRepository::class,
                )

                // when
                context.refresh()

                // then
                context.beanFactory.containsSingleton("DefaultOrderService") shouldBe true
                context.beanFactory.containsSingleton("DefaultOrderRepository") shouldBe true
            }

            test("refresh 때 주입된 빈과 조회한 빈은 같은 인스턴스여야 한다.") {
                // given
                val context = GenericApplicationContext(
                    DefaultOrderService::class,
                    DefaultOrderRepository::class,
                )
                context.refresh()

                // when & then
                context.getBean(DefaultOrderService::class).orderRepository shouldBeSameInstanceAs
                    context.getBean(OrderRepository::class)
            }

            test("getBean은 내부 beanFactory에 위임해야 한다.") {
                // given
                val context = GenericApplicationContext(
                    DefaultOrderService::class,
                    DefaultOrderRepository::class,
                )

                // when & then
                context.getBean(OrderService::class) shouldBeSameInstanceAs
                    context.beanFactory.getBean(OrderService::class)
            }
        },
    )
