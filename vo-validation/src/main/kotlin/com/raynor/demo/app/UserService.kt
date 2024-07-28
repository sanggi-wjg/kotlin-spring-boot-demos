package com.raynor.demo.app

import com.raynor.demo.app.dto.UserCreationRequest
import com.raynor.demo.app.types.UserId
import com.raynor.demo.app.types.UserName
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

    fun getUser(userId: UserId): UserEntity? {
        return userRepository.findById(userId.value)
            ?: throw IllegalArgumentException("User not found")
    }

    fun getUserByUserName(userName: UserName): UserEntity? {
        return userRepository.findByUserName(userName.value)
            ?: throw IllegalArgumentException("User not found")
    }
}