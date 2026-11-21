package com.raynor.demo.gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Component
class CustomGlobalFilter : GlobalFilter, Ordered {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getOrder(): Int = -1

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        val request = exchange.request.mutate()
            .header("X-Gateway-Finger-Print", UUID.randomUUID().toString())
            .header("X-Gateway-Requested-At", Instant.now().toString())
            .build()

        logger.info(
            ">>> [{}] {} {} | queryParams={} | headers={}",
            request.id,
            request.method,
            request.uri,
            request.queryParams,
            request.headers,
        )
        val mutatedRequest = exchange.mutate().request(request).build()

        return chain.filter(mutatedRequest).then(
            Mono.fromRunnable {
                logger.info(
                    "<<< [{}] {} | committed={} | headers={}",
                    request.id,
                    exchange.response.statusCode,
                    exchange.response.isCommitted,
                    exchange.response.headers,
                )
            },
        )
    }
}
