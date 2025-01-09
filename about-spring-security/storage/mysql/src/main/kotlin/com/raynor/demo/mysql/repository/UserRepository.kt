package com.raynor.demo.mysql.repository

import com.raynor.demo.mysql.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Int> {
    fun findByName(name: String): UserEntity?
    fun existsByName(name: String): Boolean
}
