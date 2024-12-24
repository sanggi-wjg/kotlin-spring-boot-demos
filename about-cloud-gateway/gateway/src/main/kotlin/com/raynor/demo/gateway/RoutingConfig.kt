package com.raynor.demo.gateway

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.path
import java.util.UUID

@Configuration
class RoutingConfig {

    companion object {
        const val ORDER_SERVICE_ID = "order-service"
        const val ORDER_SERVICE_PATH = "/orders/**"

        const val TASK_SERVICE_ID = "task-service"
        const val TASK_SERVICE_PATH = "/tasks/**"

        const val HEADER_X_GATEWAY_ID = "X-Gateway-Id"
        const val HEADER_X_FINGER_PRINT = "X-Finger-Print"
    }

    @Bean
    fun gatewayRoutes(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route(ORDER_SERVICE_ID) { r ->
                r.path(ORDER_SERVICE_PATH)
                    .filters {
                        it.addRequestHeadersIfNotPresent("$HEADER_X_GATEWAY_ID:$ORDER_SERVICE_ID")
                            .addRequestHeader(HEADER_X_FINGER_PRINT, UUID.randomUUID().toString())
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