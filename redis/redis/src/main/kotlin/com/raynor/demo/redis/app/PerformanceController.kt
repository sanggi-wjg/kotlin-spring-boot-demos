package com.raynor.demo.redis.app

import com.raynor.demo.redis.app.service.PerformanceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/perf")
class PerformanceController(
    private val performanceService: PerformanceService
) {

    @GetMapping("/1")
    fun perform(): ResponseEntity<Unit> {
        performanceService.performTask()
        return ResponseEntity.accepted().build()
    }
}