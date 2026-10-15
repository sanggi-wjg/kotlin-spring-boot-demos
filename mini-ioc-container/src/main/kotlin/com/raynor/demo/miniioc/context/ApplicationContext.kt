package com.raynor.demo.miniioc.context

import com.raynor.demo.miniioc.beanfactory.BeanFactory

interface ApplicationContext : BeanFactory {
    fun refresh()
}
