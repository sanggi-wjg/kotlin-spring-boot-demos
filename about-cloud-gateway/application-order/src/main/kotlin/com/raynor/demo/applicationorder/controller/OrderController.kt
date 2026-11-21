package com.raynor.demo.applicationorder.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{id}")
    fun getOrderById(
        @RequestHeader headers: Map<String, String>,
        @RequestHeader("X-Gateway-Id", required = false, defaultValue = "") gatewayId: String,
        @RequestParam("redirect", required = false, defaultValue = "") redirect: String,
        @PathVariable id: Long,
    ): ResponseEntity<String> {
        logger.info("GatewayId: $gatewayId / Headers: $headers / OrderId: $id / Redirect: $redirect")
        return ResponseEntity.ok("order $id")
    }
}
