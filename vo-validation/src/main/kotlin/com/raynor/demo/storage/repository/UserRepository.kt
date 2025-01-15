package com.raynor.demo.storage.repository

import com.raynor.demo.storage.entity.UserEntity
import org.springframework.stereotype.Component

@Component
class UserRepository {

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

    fun findById(id: Int): UserEntity? {
        return usersOnMemory.find { it.id == id }
    }

    fun findByUserName(name: String): UserEntity? {
        return usersOnMemory.find { it.name == name }
    }
}
