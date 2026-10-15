package com.raynor.demo.miniioc.beanfactory

interface SingletonBeanRegistry {
    fun containsSingleton(beanName: String): Boolean
}
