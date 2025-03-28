package com.raynor.demo.transactionaloutbox.service

import com.raynor.demo.transactionaloutbox.model.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserService(
    private val taskService: TaskService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var userId = 1

    @Transactional
    fun createUser(): User {
        val createdAt = Instant.now()
        val user = User(
            id = userId++,
            name = "123",
            email = "123" + "@dev.com",
            createdAt = createdAt,
            updatedAt = createdAt
        )
        taskService.publishOnUserSigned(user.id)
        logger.info("User created: $user")
        return user
    }
}
