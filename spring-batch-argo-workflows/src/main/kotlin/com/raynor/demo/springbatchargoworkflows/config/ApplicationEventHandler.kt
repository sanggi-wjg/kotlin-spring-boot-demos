package com.raynor.demo.springbatchargoworkflows.config

import com.raynor.demo.springbatchargoworkflows.entity.UserEntity
import com.raynor.demo.springbatchargoworkflows.repository.UserRepository
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ApplicationEventHandler(
    private val userRepository: UserRepository,
) {
    companion object {
        const val LIMIT = 1000
    }

    @EventListener(ApplicationStartedEvent::class)
    fun onApplicationStartedEvent(event: ApplicationStartedEvent) {
        val count = userRepository.count().toInt()
        if (count < LIMIT) {
            createJohnDoes(count)
        }
    }

    private fun createJohnDoes(count: Int) {
        (1..LIMIT - count).map { i ->
            UserEntity(name = "User-$i", age = i, createdAt = Instant.now())
        }.let {
            userRepository.saveAll(it)
        }
    }
}