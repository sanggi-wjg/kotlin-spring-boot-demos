package com.raynor.demo.mysql.repository

import com.raynor.demo.mysql.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Int>
