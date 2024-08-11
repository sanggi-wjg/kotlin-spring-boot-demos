package com.raynor.demo.aboutfeign.app.http

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@FeignClient(name = "dynamicUrlClient", url = "USE_DYNAMIC_URL_HERE")
interface DynamicUrlAPI {

    @GetMapping
    fun getSomething(
        uri: URI
    ): Any
}