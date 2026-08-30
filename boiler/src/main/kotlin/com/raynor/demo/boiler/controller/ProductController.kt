package com.raynor.demo.boiler.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController {
    @GetMapping("")
    fun getProducts(): ResponseEntity<String> = ResponseEntity.ok().build()
}
