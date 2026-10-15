package com.raynor.demo.boiler.core

import com.raynor.demo.boiler.infra.payment.PaymentClient
import com.raynor.demo.boiler.infra.redis.lock.DistributedLockExecutor
import com.raynor.demo.boiler.repository.OrderRepository
import com.raynor.demo.boiler.repository.ProductRepository
import com.raynor.demo.boiler.repository.UserRepository
import com.raynor.demo.boiler.service.order.OrderFacadeService
import com.raynor.demo.boiler.service.order.OrderService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.mockk
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.registerBean

class ApplicationContextTest :
    FunSpec({
        test("GenericApplicationContext 테스트") {
            // given
            val context = GenericApplicationContext()
            // 의존성의 끝단은 가짜로 채운다
            context.registerBean<DistributedLockExecutor> { mockk() }
            context.registerBean<PaymentClient> { mockk() }
            context.registerBean<OrderRepository> { mockk() }
            context.registerBean<UserRepository> { mockk() }
            context.registerBean<ProductRepository> { mockk() }
            context.registerBean<OrderService>()
            context.registerBean<OrderFacadeService>()

            // when
            context.refresh()

            // then
            context.beanDefinitionCount shouldBe 7
            context.beanFactory.shouldBeInstanceOf<DefaultListableBeanFactory>()
            context.getBean<OrderFacadeService>() shouldBeSameInstanceAs context.getBean<OrderFacadeService>()
            context.beanFactory.getDependenciesForBean(OrderFacadeService::class.java.name) shouldContainExactlyInAnyOrder arrayOf(
                DistributedLockExecutor::class.java.name,
                PaymentClient::class.java.name,
                OrderService::class.java.name,
            )
        }
    })
