package com.raynor.demo.miniioc.beanfactory

import kotlin.reflect.KClass

interface BeanFactory {
    fun <T : Any> getBean(type: KClass<T>): T

    fun getBeanNamesForType(type: KClass<*>): Set<String>
}
