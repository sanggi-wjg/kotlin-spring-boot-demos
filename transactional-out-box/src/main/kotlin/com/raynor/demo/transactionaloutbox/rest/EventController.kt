package com.raynor.demo.transactionaloutbox.rest

import com.raynor.demo.transactionaloutbox.model.Product
import com.raynor.demo.transactionaloutbox.model.User
import com.raynor.demo.transactionaloutbox.service.ProductService
import com.raynor.demo.transactionaloutbox.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/event")
class EventController(
    private val userService: UserService,
    private val productService: ProductService,
) {

    @GetMapping("/user")
    fun createUser(): ResponseEntity<User> {
        return ResponseEntity.ok(
            userService.createUser()
        )
    }

    @GetMapping("/product/update")
    fun updateProduct(): ResponseEntity<Product> {
        return ResponseEntity.ok(
            productService.updateProduct()
        )
    }
}
