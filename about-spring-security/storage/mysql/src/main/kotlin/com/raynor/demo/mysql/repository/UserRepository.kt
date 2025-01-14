package com.raynor.demo.mysql.repository

import com.raynor.demo.mysql.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Int> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM UserEntity u JOIN DeviceEntity d ON u = d.user WHERE d.accessToken = :accessToken AND d.expiredAt > CURRENT_TIMESTAMP")
    fun findByAccessToken(accessToken: String): UserEntity?
}
