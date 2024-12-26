package com.raynor.demo.abouttransaction.rest

import com.raynor.demo.abouttransaction.service.dsl.QueryDSLService
import com.raynor.demo.abouttransaction.service.persisit.EntityManagerService
import com.raynor.demo.abouttransaction.service.transaction.BasicTransactionService
import com.raynor.demo.abouttransaction.service.transaction.PropagationService
import com.raynor.demo.abouttransaction.service.transaction.TransactionAdviceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("")
class HelloController(
    private val basicTransactionService: BasicTransactionService,
    private val entityManagerService: EntityManagerService,
    private val transactionAdviceService: TransactionAdviceService,
    private val propagationService: PropagationService,
    private val queryDSLService: QueryDSLService,
) {

    @GetMapping("/basic-1")
    fun basic1() {
        basicTransactionService.getAndUpdateList()
    }

    @GetMapping("/basic-2")
    fun basic2() {
        basicTransactionService.insertWithBasicTransaction()
    }

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