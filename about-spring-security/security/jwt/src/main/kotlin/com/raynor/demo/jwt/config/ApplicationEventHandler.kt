package com.raynor.demo.jwt.config

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
        val password = "p@ssw0rd"
        val user = userRepository.save(
            UserEntity(
                name = "admin",
                email = "admin@dev.com",
                hashedPassword = passwordEncoder.encode(password),
                isAdmin = true,
                createdAt = Instant.now(),
            )
        )
        logger.info("Default Admin user created with\nemail: ${user.email}\npassword: $password")
    }
}