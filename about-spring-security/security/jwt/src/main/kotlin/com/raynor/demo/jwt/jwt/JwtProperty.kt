package com.raynor.demo.jwt.jwt

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties

@ConditionalOnProperty(prefix = "security.jwt", name = ["secret-key"])
@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperty(
    val secretKey: String,
    val tokenValidityTime: Long,
)
