package com.raynor.demo.aboutcore

import com.raynor.demo.aboutcore.aop.AopService
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
    private val aopService: AopService,
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

    @GetMapping("/aop-1")
    fun aop(): ResponseEntity<Unit> {
        /*
2024-12-23T00:26:17.672+09:00  INFO 66864 --- [nio-8080-exec-1] c.raynor.demo.aboutcore.aop.LogAspect    : 🎯 Head, Around advice: WhoAmI com.raynor.demo.aboutcore.aop.AopService.getHello()
2024-12-23T00:26:17.672+09:00  INFO 66864 --- [nio-8080-exec-1] c.raynor.demo.aboutcore.aop.LogAspect    : 🔥 Before advice: WhoAmI com.raynor.demo.aboutcore.aop.AopService.getHello()
2024-12-23T00:26:17.673+09:00  INFO 66864 --- [nio-8080-exec-1] c.raynor.demo.aboutcore.aop.LogAspect    : 🌟 After returning advice: WhoAmI com.raynor.demo.aboutcore.aop.AopService.getHello(), result: WhoAmI(username=raynor, group=1, ip=127.0.0.1, su=true)
2024-12-23T00:26:17.673+09:00  INFO 66864 --- [nio-8080-exec-1] c.raynor.demo.aboutcore.aop.LogAspect    : 🔥 After advice: WhoAmI com.raynor.demo.aboutcore.aop.AopService.getHello()
2024-12-23T00:26:17.673+09:00  INFO 66864 --- [nio-8080-exec-1] c.raynor.demo.aboutcore.aop.LogAspect    : 🎯 Tail, Around advice: WhoAmI com.raynor.demo.aboutcore.aop.AopService.getHello(), result: WhoAmI(username=raynor, group=1, ip=127.0.0.1, su=true)
        * * */
        aopService.getHello()
        return ResponseEntity.ok().build()
    }

    @GetMapping("/aop-2")
    fun aop2(): ResponseEntity<Unit> {
        /*
2024-12-23T00:26:27.701+09:00  INFO 66864 --- [nio-8080-exec-8] c.raynor.demo.aboutcore.aop.LogAspect    : 🎯 Head, Around advice: String com.raynor.demo.aboutcore.aop.AopService.getHelloOrThrow()
2024-12-23T00:26:27.702+09:00  INFO 66864 --- [nio-8080-exec-8] c.raynor.demo.aboutcore.aop.LogAspect    : 🔥 Before advice: String com.raynor.demo.aboutcore.aop.AopService.getHelloOrThrow()
2024-12-23T00:26:27.702+09:00  INFO 66864 --- [nio-8080-exec-8] c.raynor.demo.aboutcore.aop.LogAspect    : 🔥 After advice: String com.raynor.demo.aboutcore.aop.AopService.getHelloOrThrow()
2024-12-23T00:26:27.702+09:00  WARN 66864 --- [nio-8080-exec-8] c.raynor.demo.aboutcore.aop.LogAspect    : 🎯 Tail, Around advice: String com.raynor.demo.aboutcore.aop.AopService.getHelloOrThrow(). exception: java.lang.IllegalStateException: something went wrong
        * */
        aopService.getHelloOrThrow()
        return ResponseEntity.accepted().build()
    }

    @GetMapping("/aop-3")
    fun aop3(): ResponseEntity<String> {
        /*
        /* query comment is here */
        and query is below maybe.
        SELECT *
        FROM users
        WHERE id = 1
        * * */
        return aopService.fetchByQueryBuilder().let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/aop-cb")
    fun aopCallback(): ResponseEntity<Boolean> {
        aopService.testCallbackAOP()
        return ResponseEntity.ok(true)
    }
}