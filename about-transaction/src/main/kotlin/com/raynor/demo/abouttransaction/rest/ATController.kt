package com.raynor.demo.abouttransaction.rest

import com.raynor.demo.abouttransaction.service.EntityManagerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/at")
class ATController(
    private val entityManagerService: EntityManagerService,
) {

    @GetMapping("/1")
    fun permanenceTest1() {
        entityManagerService.permanenceTest1()
    }
}