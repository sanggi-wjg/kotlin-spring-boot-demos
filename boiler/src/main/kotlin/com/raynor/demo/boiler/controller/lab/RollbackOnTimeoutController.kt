package com.raynor.demo.boiler.controller.lab

import com.raynor.demo.boiler.service.lab.RollbackOnTimeoutFacadeService
import com.raynor.demo.boiler.service.lab.RollbackOnTimeoutService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lab/test")
class RollbackOnTimeoutController(
    private val rollbackOnTimeoutService: RollbackOnTimeoutService,
    private val rollbackOnTimeoutFacadeService: RollbackOnTimeoutFacadeService,
) {
    @GetMapping("/transaction-a")
    fun transactionA(): ResponseEntity<Unit> {
        rollbackOnTimeoutService.transactionA()
        return ResponseEntity.ok().build()
    }

    @GetMapping("/transaction-b")
    fun transactionB(): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.ok(rollbackOnTimeoutFacadeService.transactionB())
    }
}
