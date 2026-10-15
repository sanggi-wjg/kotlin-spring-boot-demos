package com.raynor.demo.miniioc.beanfactory.exception

class BeanDefinitionOverrideException(
    beanName: String,
) : RuntimeException("Cannot register bean definition $beanName")
