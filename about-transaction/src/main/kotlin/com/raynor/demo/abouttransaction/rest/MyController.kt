package com.raynor.demo.abouttransaction.rest

import com.raynor.demo.abouttransaction.service.EntityManagerService
import com.raynor.demo.abouttransaction.service.PropagationService
import com.raynor.demo.abouttransaction.service.QueryDSLService
import com.raynor.demo.abouttransaction.service.TransactionAdviceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("")
class MyController(
    private val entityManagerService: EntityManagerService,
    private val transactionAdviceService: TransactionAdviceService,
    private val propagationService: PropagationService,
    private val queryDSLService: QueryDSLService,
) {

    @GetMapping("/e1")
    fun permanenceSimple() {
        entityManagerService.permanenceSimple()
    }

    @GetMapping("/e2")
    fun permanenceWithSimpleQueryDsl() {
        entityManagerService.permanenceWitSimpleQueryDsl()
    }

    @GetMapping("/c-1")
    fun copySimple() {
        entityManagerService.copyEntity()
    }

    @GetMapping("/t1")
    fun something() {
        transactionAdviceService.something()
    }

    @GetMapping("/p1")
    fun propagationTest() {
        propagationService.createProductEachTransaction(raise = true)
    }

    @GetMapping("/p2")
    fun propagationTest2() {
        propagationService.createProductOneTransaction(raise = true)
    }

    @GetMapping("/q1")
    fun queryDSLTest() {
        queryDSLService.testProjection()
    }
}