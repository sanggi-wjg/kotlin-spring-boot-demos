package com.raynor.demo.app

import com.raynor.demo.app.dto.UserCreationRequest
import com.raynor.demo.app.types.UserId
import com.raynor.demo.app.types.UserName
import org.springframework.stereotype.Service

@Service
class UserService {

    private val usersOnMemory by lazy {
        listOf(
            UserEntity(
                id = 1,
                email = "abc@dev.com",
                name = "abc",
                age = 10
            ),
            UserEntity(
                id = 2,
                email = "bbb@dev.com",
                name = "bbb",
                age = 20
            )
        )
    }

    fun createUser(request: UserCreationRequest): UserEntity {
        return UserEntity(
            id = 1,
            email = request.email.value,
            name = request.name.value,
            age = request.age.value,
        )
    }

    fun getUser(userId: UserId): UserEntity? {
        return usersOnMemory.find { it.id == userId.value }
            ?: throw IllegalArgumentException("User not found")
    }

    fun getUserByUserName(userName: UserName): UserEntity? {
        return usersOnMemory.find { it.name == userName.value }
            ?: throw IllegalArgumentException("User not found")
    }
}