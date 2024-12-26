package com.raynor.demo.aboutcore.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.reflect.full.memberFunctions

@Aspect
@Component
class CallbackAspect {

    private val logger = LoggerFactory.getLogger(this::class.java)

//    @Pointcut("@annotation(com.raynor.demo.aboutcore.aop.Callback)")
//    fun callbackMethod() {
//    }

    @Around("@annotation(callback)")
    fun aroundAdvice(
        joinPoint: ProceedingJoinPoint,
        callback: Callback,
    ): Any? {
        val signature = joinPoint.signature as MethodSignature
        val klass = joinPoint.target
        val method = signature.method
        logger.info("🎯CallbackAspect: method: $method, callbackMethod: ${callback.callbackMethod}")

        val result = joinPoint.proceed()

        if (callback.callbackMethod.isNotBlank()) {
            klass::class.memberFunctions.find { it.name == callback.callbackMethod }?.also {
                it.call(klass, result)
            }
        }
        return result
    }
}
