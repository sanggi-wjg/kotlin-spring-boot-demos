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
@RequestMapping("/api/v1/orders")
class OrderController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{orderId}")
    fun getOrderById(
        @RequestHeader headers: Map<String, String>,
        @RequestHeader("X-Gateway-Finger-Print", required = false, defaultValue = "") gatewayFingerprint: String,
        @RequestParam("redirect", required = false, defaultValue = "") redirect: String,
        @PathVariable orderId: Long,
    ): ResponseEntity<String> {
        logger.info(
            ">>> [{}] orderId={} | redirect={} | headers={}",
            gatewayFingerprint,
            orderId,
            redirect,
            headers,
        )
        return ResponseEntity.ok("Hello, Order $orderId")
    }
}
