package com.raynor.demo.aboutcore.packages

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Lazy
@Service
class LazyService {
    /*
     * @Lazy 상관없이 beanDefinitionNames 에는 이미 등록 되어 있음
     * ->  BeanFactory::doGetBeanNamesForType() 에서 등록하는 것으로 보임
     *
     * beanDefinitionMap 에도 등록 되어 있음
     *
     * */

    fun uAreLazy(): String {
        return "fu"
    }
}