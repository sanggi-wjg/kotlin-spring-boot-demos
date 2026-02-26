package com.raynor.demo.springbatchargoworkflows.repository

import com.raynor.demo.springbatchargoworkflows.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Int>
