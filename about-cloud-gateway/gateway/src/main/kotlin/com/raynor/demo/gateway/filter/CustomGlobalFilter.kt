package com.raynor.demo.gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class CustomGlobalFilter :
    AbstractGatewayFilterFactory<CustomGlobalFilter.Config>(Config::class.java),
    Ordered {

    private val logger = LoggerFactory.getLogger(this::class.java)

    class Config(
        val preLog: Boolean = true,
        val postLog: Boolean = true,
    )

    override fun getOrder(): Int = -1

    override fun apply(config: Config): GatewayFilter =
        GatewayFilter { exchange, chain ->
            val request = exchange.request.mutate()
                .header("X-Gateway-Finger-Print", UUID.randomUUID().toString())
                .header("X-Gateway-Requested-At", Instant.now().toString())
                .build()

            if (config.preLog) {
                exchange.request.also { request ->
                    logger.info("request: ${request.id}, ${request.method}, ${request.uri}, ${request.queryParams}\n${request.headers}")
                }
            }
            if (config.postLog) {
                exchange.response.also { response ->
                    logger.info("response: ${response.statusCode} ${response.isCommitted}\n${response.headers}")
                }
            }
            chain.filter(exchange.mutate().request(request).build())
        }
}
