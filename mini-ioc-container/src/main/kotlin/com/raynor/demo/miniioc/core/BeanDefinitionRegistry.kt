package com.raynor.demo.miniioc.core

import kotlin.reflect.KClass

interface BeanDefinitionRegistry {
    val beanDefinitionMap: Map<String, KClass<*>>
}
