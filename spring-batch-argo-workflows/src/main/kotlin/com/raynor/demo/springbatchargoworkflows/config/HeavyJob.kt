package com.raynor.demo.springbatchargoworkflows.config

/**
 * 이 어노테이션이 붙은 Job @Bean은 heavy JobOperator로 실행됩니다.
 * 어노테이션이 없으면 기본적으로 light JobOperator가 사용됩니다.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class HeavyJob