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
    fun getUsers(): ResponseEntity<String> {
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

        return ResponseEntity.ok("hello world")
    }
}
