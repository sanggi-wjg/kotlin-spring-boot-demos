package com.raynor.demo.aboutcore.aop

import org.springframework.stereotype.Service

@Service
class AopService(
    private val customQueryBuilder: CustomQueryBuilder,
) {

    data class WhoAmI(
        val username: String,
        val group: Int,
        val ip: String,
        val su: Boolean
    )

    fun getHello(): WhoAmI {
        return WhoAmI(
            username = "raynor",
            group = 1,
            ip = "127.0.0.1",
            su = true
        )
    }

    fun getHelloOrThrow(): String {
        throw IllegalStateException("something went wrong")
    }

    fun fetchByQueryBuilder(): String {
        return customQueryBuilder
            .select("*")
            .from("users")
            .where("id = 1")
            .build()
    }
}