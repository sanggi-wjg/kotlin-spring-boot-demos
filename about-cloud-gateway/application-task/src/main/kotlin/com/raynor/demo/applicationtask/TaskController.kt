package com.raynor.demo.applicationtask

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/task")
class TaskController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{taskId}")
    fun getTaskById(
        @RequestHeader headers: Map<String, String>,
        @RequestHeader("X-Gateway-Id", required = false, defaultValue = "") gatewayId: String,
        @RequestParam("redirect", required = false, defaultValue = "") redirect: String,
        @PathVariable taskId: Long,
    ): ResponseEntity<String> {
        logger.info("GatewayId: $gatewayId / Headers: $headers / OrderId: $taskId / Redirect: $redirect")
        return ResponseEntity.ok("Hello, Task: $taskId")
    }
}
