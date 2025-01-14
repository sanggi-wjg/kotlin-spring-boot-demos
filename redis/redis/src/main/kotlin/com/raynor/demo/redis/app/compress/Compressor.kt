package com.raynor.demo.redis.app.compress

import net.jpountz.lz4.LZ4Factory
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

interface Compressor {
    fun compress(data: String): ByteArray
    fun decompress(data: ByteArray): String
}

@Component(Lz4Compressor.BEAN_NAME)
class Lz4Compressor : Compressor {

    companion object {
        const val BEAN_NAME = "lz4Compressor"
    }

    private val instance by lazy { LZ4Factory.fastestInstance() }
    private val compressor by lazy { instance.fastCompressor() }
    private val decompressor by lazy { instance.fastDecompressor() }

    override fun compress(data: String): ByteArray {
        val originalBytes = data.toByteArray(StandardCharsets.UTF_8)
        val maxCompressedLength = compressor.maxCompressedLength(originalBytes.size)
        val compressedBytes = ByteArray(maxCompressedLength)
        compressor.compress(originalBytes, 0, originalBytes.size, compressedBytes, 0, maxCompressedLength)
        return compressedBytes
    }

    override fun decompress(data: ByteArray): String {
        val restoredBytes = ByteArray(data.size + 255) // 안전을 위해 충분히 큰 배열 할당
        val decompressedLength = decompressor.decompress(data, 0, restoredBytes, 0, restoredBytes.size)
        return restoredBytes.toString(StandardCharsets.UTF_8)
    }
}