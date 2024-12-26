package com.raynor.demo.gateway

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.util.UUID

@Configuration
class RoutingConfig {

    companion object {
        const val ORDER_SERVICE_ID = "order-service"
        const val ORDER_SERVICE_PATH = "/orders/**"

        const val TASK_SERVICE_ID = "task-service"
        const val TASK_SERVICE_PATH = "/tasks/**"

        const val HEADER_X_GATEWAY_ID = "X-Gateway-Id"
        const val HEADER_X_GATEWAY_FINGER_PRINT = "X-Gateway-Finger-Print"
        const val HEADER_X_GATEWAY_REQUESTED_AT = "X-Gateway-Requested-At"
    }

    @Bean
    fun gatewayRoutes(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route(ORDER_SERVICE_ID) { r ->
                r.path(ORDER_SERVICE_PATH)
                    .filters { spec ->
                        spec.addRequestHeader(HEADER_X_GATEWAY_FINGER_PRINT, UUID.randomUUID().toString())
                            .addRequestHeader(HEADER_X_GATEWAY_REQUESTED_AT, Instant.now().toString())
                            .addRequestHeadersIfNotPresent("$HEADER_X_GATEWAY_ID:$ORDER_SERVICE_ID")
                            .addRequestParameter("redirect", "https://google.com")
                    }
                    .uri("http://localhost:8081")
            }
            .route(TASK_SERVICE_ID) { r ->
                r.path(TASK_SERVICE_PATH)
                    .uri("http://localhost:8082")
            }
            .build()
    }
}