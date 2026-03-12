package com.raynor.demo.k8sqos.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sun.misc.Unsafe
import java.nio.ByteBuffer

@RestController
@RequestMapping("/stress")
class StressController {

    private val heapChunks: MutableList<ByteArray> = mutableListOf()
    private val directBuffers: MutableList<ByteBuffer> = mutableListOf()
    private val nativeAddresses: MutableList<Long> = mutableListOf()

    private val unsafe: Unsafe = run {
        val field = Unsafe::class.java.getDeclaredField("theUnsafe")
        field.isAccessible = true
        field.get(null) as Unsafe
    }

    @GetMapping("/heap")
    fun heapStress(
        @RequestParam(defaultValue = "1") sizeInMB: Int,
        @RequestParam(defaultValue = "100") delayMs: Long,
        @RequestParam(defaultValue = "100") repeat: Int,
    ): String {
        val chunkSize = sizeInMB * 1024 * 1024
        var count = 0

        try {
            repeat(repeat) {
                heapChunks.add(ByteArray(chunkSize))
                count++
                Thread.sleep(delayMs)
            }
        } catch (_: OutOfMemoryError) {
            return "Heap OOM after allocating $count chunks (${count * sizeInMB} MB). Total held: ${heapChunks.size} chunks"
        }

        return "Allocated $count chunks (${count * sizeInMB} MB). Total held: ${heapChunks.size} chunks"
    }

    @GetMapping("/direct")
    fun directBufferStress(
        @RequestParam(defaultValue = "1") sizeInMB: Int,
        @RequestParam(defaultValue = "100") delayMs: Long,
        @RequestParam(defaultValue = "100") repeat: Int,
    ): String {
        val chunkSize = sizeInMB * 1024 * 1024
        var count = 0

        try {
            repeat(repeat) {
                directBuffers.add(ByteBuffer.allocateDirect(chunkSize))
                count++
                Thread.sleep(delayMs)
            }
        } catch (_: OutOfMemoryError) {
            return "Direct buffer OOM after allocating $count buffers (${count * sizeInMB} MB). Total held: ${directBuffers.size} buffers"
        }

        return "Allocated $count direct buffers (${count * sizeInMB} MB). Total held: ${directBuffers.size} buffers"
    }

    @GetMapping("/native")
    fun nativeMemoryStress(
        @RequestParam(defaultValue = "1") sizeInMB: Int,
        @RequestParam(defaultValue = "100") delayMs: Long,
        @RequestParam(defaultValue = "100") repeat: Int,
    ): String {
        val chunkSize = sizeInMB.toLong() * 1024 * 1024
        var count = 0

        try {
            repeat(repeat) {
                val address = unsafe.allocateMemory(chunkSize)
                unsafe.setMemory(address, chunkSize, 1)
                nativeAddresses.add(address)
                count++
                Thread.sleep(delayMs)
            }
        } catch (_: OutOfMemoryError) {
            return "Native OOM after allocating $count blocks (${count * sizeInMB} MB). Total held: ${nativeAddresses.size} blocks"
        }

        return "Allocated $count native blocks (${count * sizeInMB} MB). Total held: ${nativeAddresses.size} blocks"
    }

    @GetMapping("/clear")
    fun clear(): String {
        val heapCount = heapChunks.size
        val directCount = directBuffers.size
        val nativeCount = nativeAddresses.size

        heapChunks.clear()
        directBuffers.clear()

        nativeAddresses.forEach { address ->
            try {
                unsafe.freeMemory(address)
            } catch (_: Exception) {
            }
        }
        nativeAddresses.clear()
        System.gc()
        return "Cleared heap=$heapCount chunks, direct=$directCount buffers, native=$nativeCount arenas. GC requested."
    }
}
