package com.raynor.demo.miniioc.exceptions

class NoSuchBeanDefinitionException(
    beanName: String,
) : RuntimeException("No bean named $beanName")
