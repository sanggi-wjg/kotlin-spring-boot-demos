package com.raynor.demo.miniioc.beanfactory

import com.raynor.demo.miniioc.beanfactory.exception.BeanDefinitionOverrideException
import com.raynor.demo.miniioc.beanfactory.exception.NoSuchBeanDefinitionException
import com.raynor.demo.miniioc.beanfactory.exception.NoUniqueBeanDefinitionException
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

class DefaultBeanFactory(
    vararg classes: KClass<*>,
) : BeanFactory,
    BeanDefinitionRegistry,
    SingletonBeanRegistry {

    private val singletonObjects = mutableMapOf<String, Any>()

    override val beanDefinitionMap: Map<String, KClass<*>> = buildMap {
        classes.forEach { type ->
            val beanName = type.simpleName!!
            if (beanName in this) throw BeanDefinitionOverrideException(beanName)
            put(beanName, type)
        }
    }

    /** 타입으로 빈을 찾는다. 인터페이스로 찾아도 되고, 처음 찾는 빈이면 만들어서 캐시에 넣어 둔다. */
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

    /** 이 타입으로 꺼낼 수 있는 빈 이름을 모두 찾는다. */
    override fun getBeanNamesForType(type: KClass<*>): Set<String> {
        return beanDefinitionMap.filter { it.value.isSubclassOf(type) }.keys
    }

    /** 이미 만들어 둔 빈인지 확인한다. */
    override fun containsSingleton(beanName: String): Boolean {
        return singletonObjects.containsKey(beanName)
    }

    /** 등록된 빈을 전부 미리 만들어 둔다. */
    fun preInstantiateSingletons() {
        beanDefinitionMap.values.forEach { getBean(it) }
    }

    /** 주 생성자로 빈을 만든다. 생성자에 필요한 빈은 getBean으로 가져온다. */
    private fun createBean(beanName: String): Any {
        val constructor = beanDefinitionMap.getValue(beanName).primaryConstructor!!
        val arguments = constructor.parameters.map { getBean(it.type.classifier as KClass<*>) }
        return constructor.call(*arguments.toTypedArray())
    }
}
