package com.raynor.demo.miniioc.context

import com.raynor.demo.miniioc.beanfactory.DefaultBeanFactory
import kotlin.reflect.KClass

class GenericApplicationContext(
    vararg classes: KClass<*>,
) : ApplicationContext {

    val beanFactory: DefaultBeanFactory = DefaultBeanFactory(*classes)

    override fun <T : Any> getBean(type: KClass<T>): T {
        return this.beanFactory.getBean(type)
    }

    override fun getBeanNamesForType(type: KClass<*>): Set<String> {
        return this.beanFactory.getBeanNamesForType(type)
    }

    /** 빈을 미리 다 만들어 둔다. 실제 일은 beanFactory가 한다. */
    override fun refresh() {
        this.beanFactory.preInstantiateSingletons()
    }
}
