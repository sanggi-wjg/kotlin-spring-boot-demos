package com.raynor.demo.miniioc.exceptions

class NoUniqueBeanDefinitionException(
    beanName: String,
    foundBeanNames: Set<String>,
) : RuntimeException("No unique bean definition found for bean name '$beanName', found: $foundBeanNames")
