package com.raynor.demo.boiler.repository

import com.raynor.demo.boiler.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Int>
