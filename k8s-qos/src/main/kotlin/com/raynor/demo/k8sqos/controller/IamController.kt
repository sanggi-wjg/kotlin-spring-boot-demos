package com.raynor.demo.k8sqos.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/iam")
class IamController {

    @RequestMapping("")
    fun hello() = "Hello World!"
}