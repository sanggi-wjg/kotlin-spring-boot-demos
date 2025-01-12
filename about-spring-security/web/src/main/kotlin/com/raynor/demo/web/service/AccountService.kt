package com.raynor.demo.web.service

import com.raynor.demo.jwt.jwt.JwtHelper
import com.raynor.demo.jwt.model.CustomUserDetails
import com.raynor.demo.mysql.entity.DeviceEntity
import com.raynor.demo.mysql.entity.UserEntity
import com.raynor.demo.mysql.repository.UserRepository
import com.raynor.demo.web.controller.dto.auth.EmailLoginRequestDto
import com.raynor.demo.web.controller.dto.auth.EmailSignupRequestDto
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
@Service
class AccountService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtHelper: JwtHelper,
    private val authenticationManager: AuthenticationManager,
) {

    fun emailSignup(emailSignupRequestDto: EmailSignupRequestDto): UserEntity {
        // implement idempotent logic with idempotency-key and unique constraint in production
        if (userRepository.existsByEmail(emailSignupRequestDto.email)) {
            throw RuntimeException("email already exists")
        }

        return userRepository.save(emailSignupRequestDto.toUserEntity())
    }

    fun emailLogin(emailLoginRequestDto: EmailLoginRequestDto): DeviceEntity {
        // implement caching logic with cache strategy in production
        val user = userRepository.findByEmail(emailLoginRequestDto.email)
            ?: throw EntityNotFoundException("${emailLoginRequestDto.email} not found")

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(emailLoginRequestDto.email, emailLoginRequestDto.password)
        ) as CustomUserDetails
        val accessToken = jwtHelper.generateToken(authentication)
        val refreshToken = jwtHelper.generateRefreshToken(authentication)

        return DeviceEntity(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userAgent = "whatever",
            createdAt = Instant.now(),
            expiredAt = jwtHelper.getExpirationBy(accessToken)!!.toInstant(),
            user = user,
        ).let {
            user.addDevice(it)
            it
        }
    }

    private fun EmailSignupRequestDto.toUserEntity(): UserEntity {
        return UserEntity(
            name = name,
            email = email,
            hashedPassword = passwordEncoder.encode(password),
            isAdmin = false,
            createdAt = Instant.now(),
        )
    }
}