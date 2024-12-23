package com.raynor.demo.aboutcore.aop

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class LogAspect {

    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        const val QUERY_BUILD = "com.raynor.demo.aboutcore.aop.CustomQueryBuilder.build*(..))"
    }

    //    @Before("execution(* com.raynor.demo.aboutcore.scope.PrototypeScopeService.getTimes())")
    @Before("execution(* com.raynor.demo.aboutcore.aop.*.*(..))")
    fun beforeAdvice(joinPoint: JoinPoint) {
        logger.info("🔥 Before advice: ${joinPoint.signature}")
    }

    @After("execution(* com.raynor.demo.aboutcore.aop.*.*(..))")
    fun afterAdvice(joinPoint: JoinPoint) {
        logger.info("🔥 After advice: ${joinPoint.signature}")
    }

    @AfterReturning("execution(* com.raynor.demo.aboutcore.aop.*.*(..))", returning = "result")
    fun afterReturningAdvice(joinPoint: JoinPoint, result: Any?) {
        logger.info("🌟 After returning advice: ${joinPoint.signature}, result: $result")
    }

    @AfterThrowing("execution(* com.raynor.demo.aboutcore.aop.*.*(..))", throwing = "exception")
    fun afterThrowingAdvice(joinPoint: JoinPoint, throwable: Throwable) {
        logger.info("💥 After throwing advice: ${joinPoint.signature}, exception: $throwable")
    }

    @Around("execution(* com.raynor.demo.aboutcore.aop.*.*(..))")
    fun aroundAdvice(joinPoint: ProceedingJoinPoint): Any? {
        logger.info("🎯 Head, Around advice: ${joinPoint.signature}")

        return runCatching {
            joinPoint.proceed()
        }.onFailure {
            // 일반적인 경우가 아니라면 사용하면 안되겠네. 에러나면 걍 에러나도록 하든가
            // 필요한 경우 아니라면? API 클라 구현단? 에서는 할만한가??
            logger.warn("🎯 Tail, Around advice: ${joinPoint.signature}. exception: $it")
        }.onSuccess {
            logger.info("🎯 Tail, Around advice: ${joinPoint.signature}, result: $it")
        }.getOrNull()
    }

    @Around("execution(* $QUERY_BUILD")
    fun customJPAQueryFactoryAdvice(joinPoint: ProceedingJoinPoint): String {
        return (joinPoint.proceed() as String).setComment()
    }
}