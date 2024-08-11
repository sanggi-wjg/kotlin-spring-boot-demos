package com.raynor.demo.aboutfeign.app

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/seek")
class SeekController(
    private val seekService: SeekService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @RequestMapping("/1")
    fun get(): ResponseEntity<String> {
        seekService.getUsers().let {
            logger.info("111 $it")
        }

        try {
            seekService.getUsersUnHandledEnum().let {
                logger.info("222 $it")
            }
        } catch (_: Exception) {
        }

        seekService.getUsersUnHandledEnumButNoRaise().let {
            logger.info("333 $it")
        }

        return ResponseEntity.ok("hello world - GET")
    }

    @RequestMapping("/2")
    fun post(): ResponseEntity<String> {
        seekService.createUser()
        return ResponseEntity.ok("hello world - POST")
    }

    @RequestMapping("/3")
    fun put(): ResponseEntity<String> {
        seekService.updateUser()
        return ResponseEntity.ok("hello world - PATCH")
    }

    @RequestMapping("/4")
    fun delete(): ResponseEntity<String> {
        seekService.deleteUser()
        return ResponseEntity.ok("hello world - DELETE")
    }

    @RequestMapping("/5")
    fun dynamicUrl(): ResponseEntity<String> {
        seekService.requestWithDynamicURL()
        return ResponseEntity.ok("hello dynamic url")
    }
}
