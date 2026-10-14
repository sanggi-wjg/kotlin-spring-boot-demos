package com.raynor.demo.miniioc.core

import com.raynor.demo.miniioc.exceptions.BeanDefinitionOverrideException
import com.raynor.demo.miniioc.exceptions.NoSuchBeanDefinitionException
import com.raynor.demo.miniioc.exceptions.NoUniqueBeanDefinitionException
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

class DefaultBeanFactory(
    vararg classes: KClass<*>,
) : BeanFactory,
    BeanDefinitionRegistry {

    override val beanDefinitionMap: Map<String, KClass<*>> = run {
        val beanMap = mutableMapOf<String, KClass<*>>()
        classes.forEach { type ->
            val beanName = type.simpleName!!
            if (beanMap.containsKey(beanName)) {
                throw BeanDefinitionOverrideException(beanName)
            }
            beanMap[beanName] = type
        }
        beanMap
    }

    private val singletonObjects = mutableMapOf<String, Any>()

    override fun <T : Any> getBean(type: KClass<T>): T {
        val beanName = type.simpleName!!
        val beanNames = getBeanNamesForType(type)
        if (beanNames.isEmpty()) throw NoSuchBeanDefinitionException(beanName)
        if (beanNames.size > 1) throw NoUniqueBeanDefinitionException(beanName, beanNames)

        val targetBeanName = beanNames.first()
        val bean = singletonObjects.getOrPut(targetBeanName) {
            createBean(targetBeanName)
        }
        return type.cast(bean)
    }

    private fun createBean(beanName: String): Any {
        return beanDefinitionMap.getValue(beanName).createInstance()
    }

    override fun getBeanNamesForType(type: KClass<*>): Set<String> {
        val beans = beanDefinitionMap.filter { it.value.isSubclassOf(type) }
        return beans.keys
    }
}
