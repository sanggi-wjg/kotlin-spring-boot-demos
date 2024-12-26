package com.raynor.demo.aboutcore.aop

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
annotation class Callback(
    val name: String,
    val callbackMethod: String = "",
)
