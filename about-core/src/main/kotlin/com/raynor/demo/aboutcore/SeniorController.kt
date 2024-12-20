package com.raynor.demo.aboutcore

import com.raynor.demo.aboutcore.product.ProductSearchService
import com.raynor.demo.aboutcore.scope.PrototypeScopeService
import com.raynor.demo.aboutcore.scope.RequestScopeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SeniorController(
    private val productSearchService: ProductSearchService,
    private val prototypeScopeService: PrototypeScopeService,
    private val requestScopeService: RequestScopeService,
) {

    @GetMapping("/products")
    fun getProducts(): ResponseEntity<List<String>> {
        return productSearchService.getProducts().let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/proto")
    fun aPrototypeScope(): ResponseEntity<Boolean> {
        return prototypeScopeService.getTimes().let {
            ResponseEntity.ok(true)
        }
    }

    @GetMapping("/request-scope")
    fun aRequestScope(): ResponseEntity<List<String?>> {
        return requestScopeService.getContext().let {
            ResponseEntity.ok(it)
        }
    }
}