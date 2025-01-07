package com.raynor.demo.web.config

import com.raynor.demo.mysql.entity.UserEntity
import com.raynor.demo.mysql.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ApplicationEventHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    private val logger = LoggerFactory.getLogger(ApplicationEventHandler::class.java)

    @EventListener(ApplicationStartedEvent::class)
    fun applicationStarted(event: ApplicationStartedEvent) {
        val email = "admin@dev.com"
        val password = "p@ssw0rd"

        userRepository.save(
            UserEntity(
                name = "admin",
                email = email,
                hashedPassword = passwordEncoder.encode(password),
                createdAt = Instant.now(),
            )
        )
        logger.info("Default Admin user created with\nemail: $email\npassword: $password")
    }
}