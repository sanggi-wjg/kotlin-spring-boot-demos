package com.raynor.demo.applicationorder

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{id}")
    fun getOrderById(
        @RequestHeader("X-Gateway-Id", required = false, defaultValue = "") gatewayId: String,
        @RequestHeader("X-Finger-Print", required = false, defaultValue = "") fingerPrint: String,
        @RequestHeader headers: Map<String, String>,
        @PathVariable id: Long
    ): ResponseEntity<String> {
        logger.info("get order by id: $id, gatewayId: $gatewayId, fingerPrint: $fingerPrint\nheaders: $headers")
        return ResponseEntity.ok("order $id")
    }
}