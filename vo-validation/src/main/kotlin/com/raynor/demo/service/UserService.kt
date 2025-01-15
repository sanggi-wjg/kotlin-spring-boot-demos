package com.raynor.demo.service

import com.raynor.demo.controller.request.UserCreationRequest
import com.raynor.demo.domain.types.UserId
import com.raynor.demo.storage.entity.UserEntity
import com.raynor.demo.storage.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    fun createUser(request: UserCreationRequest): UserEntity {
        return UserEntity(
            id = 1,
            email = request.email.value,
            name = request.name.value,
            age = request.age.value,
        )
    }

    fun getUser(userId: UserId): UserEntity {
        return userRepository.findById(userId.value)
            ?: throw IllegalArgumentException("User not found")
    }
}
