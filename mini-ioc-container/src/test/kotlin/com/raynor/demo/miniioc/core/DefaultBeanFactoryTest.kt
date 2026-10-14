package com.raynor.demo.miniioc.core

import com.raynor.demo.miniioc.beans.DefaultOrderRepository
import com.raynor.demo.miniioc.beans.DefaultOrderService
import com.raynor.demo.miniioc.beans.JdbcOrderRepository
import com.raynor.demo.miniioc.beans.OrderRepository
import com.raynor.demo.miniioc.exceptions.BeanDefinitionOverrideException
import com.raynor.demo.miniioc.exceptions.NoSuchBeanDefinitionException
import com.raynor.demo.miniioc.exceptions.NoUniqueBeanDefinitionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class DefaultBeanFactoryTest :
    FunSpec({

        context("빈 정의") {
            test("빈 정의를 할 수 있어야 한다.") {
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderService::class,
                    DefaultOrderRepository::class,
                )

                beanFactory.beanDefinitionMap shouldBe
                    mapOf(
                        "DefaultOrderService" to DefaultOrderService::class,
                        "DefaultOrderRepository" to DefaultOrderRepository::class,
                    )
            }

            test("빈 정의시 중복이 있다면 오류") {
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
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                    JdbcOrderRepository::class,
                )

                beanFactory.getBean(DefaultOrderRepository::class).javaClass shouldBe
                    DefaultOrderRepository::class.java
            }

            test("빈 조회를 할 수 있어야 한다: 인터페이스 조회") {
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                )

                beanFactory.getBean(OrderRepository::class).javaClass shouldBe
                    DefaultOrderRepository::class.java
            }

            test("빈 조회를 여러번 해도 같은 인스턴스를 반환한다.") {
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                )

                val bean1 = beanFactory.getBean(OrderRepository::class)
                val bean2 = beanFactory.getBean(OrderRepository::class)
                val bean3 = beanFactory.getBean(DefaultOrderRepository::class)

                bean1 shouldBeSameInstanceAs bean2
                bean2 shouldBeSameInstanceAs bean3
            }

            test("빈 조회시 없다면 오류") {
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                    JdbcOrderRepository::class,
                )

                shouldThrow<NoSuchBeanDefinitionException> {
                    beanFactory.getBean(DefaultOrderService::class)
                }
            }

            test("빈 조회시 중복이 있다면 오류") {
                val beanFactory = DefaultBeanFactory(
                    DefaultOrderRepository::class,
                    JdbcOrderRepository::class,
                )

                shouldThrow<NoUniqueBeanDefinitionException> {
                    beanFactory.getBean(OrderRepository::class)
                }
            }
        }
    })
