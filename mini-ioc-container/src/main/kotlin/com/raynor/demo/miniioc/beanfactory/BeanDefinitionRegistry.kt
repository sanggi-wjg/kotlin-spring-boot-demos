package com.raynor.demo.miniioc.beanfactory

import kotlin.reflect.KClass

interface BeanDefinitionRegistry {
    val beanDefinitionMap: Map<String, KClass<*>>
}
