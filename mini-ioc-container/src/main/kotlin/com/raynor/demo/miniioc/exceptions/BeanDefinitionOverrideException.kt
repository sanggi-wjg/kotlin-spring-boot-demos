package com.raynor.demo.miniioc.exceptions

class BeanDefinitionOverrideException(
    beanName: String,
) : RuntimeException("Cannot register bean definition $beanName")
