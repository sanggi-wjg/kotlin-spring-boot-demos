package com.raynor.demo.batchbulkexcel.storage.file.config

import com.raynor.demo.batchbulkexcel.storage.file.FileStorage
import com.raynor.demo.batchbulkexcel.storage.file.LocalFileStorage
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Path

@Configuration
@EnableConfigurationProperties(LocalStorageProperties::class)
class FileStorageConfig {
    @Bean
    fun fileStorage(props: LocalStorageProperties): FileStorage = LocalFileStorage(Path.of(props.baseDir))
}

@ConfigurationProperties("storage.local")
data class LocalStorageProperties(
    val baseDir: String,
)
