package com.raynor.demo.aboutgctuning.config

import com.raynor.demo.aboutgctuning.entity.ProductEntity
import com.raynor.demo.aboutgctuning.repository.ProductRepository
import jakarta.transaction.Transactional
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

@Component
class ApplicationEventHandler(
    private val productRepository: ProductRepository
) {
    @Transactional
    @EventListener(ApplicationStartedEvent::class)
    fun onApplicationEvent(event: ApplicationStartedEvent) {
        val count = productRepository.count()
        if (count >= 100) return

        (1..100 - count).map {
            ProductEntity(
                name = UUID.randomUUID().toString(),
                price = BigDecimal(Random.nextInt(1, 100) * 100)
            )
        }.let {
            productRepository.saveAll(it)
        }
    }
}
