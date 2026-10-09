package com.raynor.demo.boiler.service.lab

import com.raynor.demo.boiler.repository.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RollbackOnTimeoutService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun transactionA() {
        val user100 = userRepository.findByIdOrNull(100)
            ?: throw EntityNotFoundException()
        user100.appendName("_@")
        userRepository.flush()

        Thread.sleep(100_000)
        throw RuntimeException("rollback")
    }

    @Transactional
    fun transactionB(): Map<String, String?> {
        val user99 = userRepository.findByIdOrNull(99)
            ?: throw EntityNotFoundException()
        user99.appendName("_@")

        val user100 = userRepository.findByIdOrNull(100)
            ?: throw EntityNotFoundException()
        user100.appendName("_@")

        return mapOf(
            "user99_name" to userRepository.findByIdOrNull(99)?.name,
            "user100_name" to userRepository.findByIdOrNull(100)?.name,
        )
    }
}
