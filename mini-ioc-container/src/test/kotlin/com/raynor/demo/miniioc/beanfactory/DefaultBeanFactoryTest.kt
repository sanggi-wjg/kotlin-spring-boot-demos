package com.raynor.demo.miniioc.beanfactory

import com.raynor.demo.miniioc.beanfactory.exception.BeanDefinitionOverrideException
import com.raynor.demo.miniioc.beanfactory.exception.NoSuchBeanDefinitionException
import com.raynor.demo.miniioc.beanfactory.exception.NoUniqueBeanDefinitionException
import com.raynor.demo.miniioc.example.DefaultOrderRepository
import com.raynor.demo.miniioc.example.DefaultOrderService
import com.raynor.demo.miniioc.example.JdbcOrderRepository
import com.raynor.demo.miniioc.example.OrderRepository
import com.raynor.demo.miniioc.example.OrderService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class DefaultBeanFactoryTest :
    FunSpec({

        context("빈 정의") {
            test("빈 정의를 할 수 있어야 한다.") {
                // when
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderService::class,
                    DefaultOrderRepository::class,
                )

                // then
                beanFactory.beanDefinitionMap shouldBe
                    mapOf(
                        "DefaultOrderService" to DefaultOrderService::class,
                        "DefaultOrderRepository" to DefaultOrderRepository::class,
                    )
            }

            test("빈 정의시 중복이 있다면 오류") {
                // when & then
                shouldThrow<BeanDefinitionOverrideException> {
                    DefaultBeanFactory(
                        DefaultOrderService::class,
                        DefaultOrderRepository::class,
                        DefaultOrderRepository::class,
                    )
                }
            }
        }

        context("빈 조회") {
            test("빈 조회를 할 수 있어야 한다.") {
                // given
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                    JdbcOrderRepository::class,
                )

                // when & then
                beanFactory.getBean(DefaultOrderRepository::class).javaClass shouldBe
                    DefaultOrderRepository::class.java
            }

            test("빈 조회를 할 수 있어야 한다: 인터페이스 조회") {
                // given
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                )

                // when & then
                beanFactory.getBean(OrderRepository::class).javaClass shouldBe
                    DefaultOrderRepository::class.java
            }

            test("빈 조회를 여러번 해도 같은 인스턴스를 반환한다.") {
                // given
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                )

                // when
                val bean1 = beanFactory.getBean(OrderRepository::class)
                val bean2 = beanFactory.getBean(OrderRepository::class)
                val bean3 = beanFactory.getBean(DefaultOrderRepository::class)

                // then
                bean1 shouldBeSameInstanceAs bean2
                bean2 shouldBeSameInstanceAs bean3
                beanFactory.containsSingleton("DefaultOrderRepository") shouldBe true
            }

            test("빈 조회시 없다면 오류") {
                // given
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                    JdbcOrderRepository::class,
                )

                // when & then
                shouldThrow<NoSuchBeanDefinitionException> {
                    beanFactory.getBean(DefaultOrderService::class)
                }
            }

            test("빈 조회시 중복이 있다면 오류") {
                // given
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                    JdbcOrderRepository::class,
                )

                // when & then
                shouldThrow<NoUniqueBeanDefinitionException> {
                    beanFactory.getBean(OrderRepository::class)
                }
            }
        }

        context("의존성 주입") {
            test("생성자 인자로 등록된 빈이 주입된다.") {
                // given
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderService::class,
                    DefaultOrderRepository::class,
                )

                // when
                val orderService = beanFactory.getBean(DefaultOrderService::class)

                // then
                orderService.orderRepository shouldBeSameInstanceAs beanFactory.getBean(OrderRepository::class)
            }

            test("주입할 빈이 없다면 오류") {
                // given
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderService::class,
                )

                // when & then
                shouldThrow<NoSuchBeanDefinitionException> {
                    beanFactory.getBean(OrderService::class)
                }
            }

            test("주입할 빈 후보가 여럿이면 오류") {
                // given
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderService::class,
                    DefaultOrderRepository::class,
                    JdbcOrderRepository::class,
                )

                // when & then
                shouldThrow<NoUniqueBeanDefinitionException> {
                    beanFactory.getBean(OrderService::class)
                }
            }
        }
    })
