package com.raynor.demo.batchbulkexcel.storage.file

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

interface FileStorage {
    fun store(
        key: String,
        content: InputStream,
    ): String

    fun presignedUrl(
        key: String,
        ttl: Duration = Duration.ofMinutes(15),
    ): String
}

class LocalFileStorage(
    baseDir: Path,
) : FileStorage {
    private val baseDir: Path = baseDir.toAbsolutePath().normalize()

    override fun store(
        key: String,
        content: InputStream,
    ): String {
        val target = resolve(key)
        Files.createDirectories(target.parent)
        content.use { input ->
            Files.newOutputStream(target).use { output -> input.copyTo(output) }
        }
        return key
    }

    override fun presignedUrl(
        key: String,
        ttl: Duration,
    ): String = resolve(key).toUri().toString()

    private fun resolve(key: String): Path {
        val target = baseDir.resolve(key).normalize()
        require(target.startsWith(baseDir)) {
            "key must stay within base dir: $key"
        }
        return target
    }
}
