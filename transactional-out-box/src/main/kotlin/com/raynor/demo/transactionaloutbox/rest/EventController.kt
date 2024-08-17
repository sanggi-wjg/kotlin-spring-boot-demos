package com.raynor.demo.transactionaloutbox.rest

import com.raynor.demo.transactionaloutbox.service.CreateTaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/event")
class EventController(
    private val createTaskService: CreateTaskService,
) {

    @RequestMapping("/user")
    fun createUser(): ResponseEntity<String> {
        createTaskService.publishOnUserSigned()
        return ResponseEntity.ok("user created")
    }

    @RequestMapping("/product/u")
    fun updateProduct(): ResponseEntity<String> {
        createTaskService.publishOnProductUpdated()
        return ResponseEntity.ok("product updated")
    }
}
