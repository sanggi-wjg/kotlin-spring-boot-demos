package com.raynor.demo.k8sqos.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stress")
class StressController {

    private val allocatedChunks: MutableList<ByteArray> = mutableListOf()

    @Volatile
    private var running = false

    @GetMapping("/oom")
    fun heapOom(
        @RequestParam(defaultValue = "1") sizeInMB: Int,
        @RequestParam(defaultValue = "100") delayMs: Long,
    ): String {
        running = true
        val chunkSize = sizeInMB * 1024 * 1024
        var count = 0

        try {
            while (running) {
                allocatedChunks.add(ByteArray(chunkSize))
                count++
                Thread.sleep(delayMs)
            }
        } catch (e: OutOfMemoryError) {
            return "OOM after allocating $count chunks (${count * sizeInMB} MB). Total held: ${allocatedChunks.size} chunks"
        }

        return "Stopped after allocating $count chunks (${count * sizeInMB} MB)"
    }

    @GetMapping("/clear")
    fun clear(): String {
        running = false
        val count = allocatedChunks.size
        allocatedChunks.clear()
        System.gc()
        return "Cleared $count chunks and requested GC"
    }
}
