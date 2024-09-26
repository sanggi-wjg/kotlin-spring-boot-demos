package com.raynor.demo.abouttransaction.rest

import com.raynor.demo.abouttransaction.service.EntityManagerService
import com.raynor.demo.abouttransaction.service.TransactionAdviceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("")
class ATController(
    private val entityManagerService: EntityManagerService,
    private val transactionAdviceService: TransactionAdviceService,
) {

    @GetMapping("/e1")
    fun permanenceSimple() {
        entityManagerService.permanenceSimple()
    }

    @GetMapping("/e2")
    fun permanenceWithSimpleQueryDsl() {
        entityManagerService.permanenceWitSimpleQueryDsl()
    }

    @GetMapping("/t1")
    fun something() {
        transactionAdviceService.something()
    }
}