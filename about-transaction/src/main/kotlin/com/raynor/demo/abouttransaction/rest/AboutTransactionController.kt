package com.raynor.demo.abouttransaction.rest

import com.raynor.demo.abouttransaction.service.EntityManagerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/at")
class AboutTransactionController(
    private val entityManagerService: EntityManagerService,
) {

    @GetMapping("/p-1")
    fun permanenceSimple() {
        entityManagerService.permanenceSimple()
    }


    @GetMapping("/p-2")
    fun permanenceWithSimpleQueryDsl() {
        entityManagerService.permanenceWitSimpleQueryDsl()
    }
}