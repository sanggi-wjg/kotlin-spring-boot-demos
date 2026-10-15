package com.raynor.demo.miniioc.beanfactory.exception

class NoSuchBeanDefinitionException(
    beanName: String,
) : RuntimeException("No bean named $beanName")
