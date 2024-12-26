package com.raynor.demo.gateway

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
class CustomGlobalFilter : AbstractGatewayFilterFactory<CustomGlobalFilter.Config>(Config::class.java), Ordered {

    private val logger = LoggerFactory.getLogger(this::class.java)

    class Config(
        val preLog: Boolean = true,
        val postLog: Boolean = true,
    )

    override fun apply(config: Config?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            if (config?.preLog == true) {
                exchange.request.also {
                    logger.info("request: ${it.id}, ${it.method}, ${it.uri}, ${it.queryParams}\n${it.headers}")
                }
            }
            if (config?.postLog == true) {
                exchange.response.also {
                    logger.info("response: ${it.statusCode} ${it.isCommitted}\n${it.headers}")
                }
            }
            chain.filter(exchange)
        }
    }

    override fun getOrder(): Int {
        return -1
    }
}
