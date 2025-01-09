package com.raynor.demo.jwt.jwt

import com.raynor.demo.jwt.model.AuthorizedUser
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtHelper(
    private val jwtProperty: JwtProperty,
) {

    fun generateToken(user: AuthorizedUser): String {
        val now = Instant.now()
        val issuedAt = Date(now.toEpochMilli())
        val expiredAt = Date(now.toEpochMilli() + jwtProperty.tokenValidityTime)

        return Jwts.builder()
            .subject(user.username)
            .issuedAt(issuedAt)
            .expiration(expiredAt)
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun decodeToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSignKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun isTokenExpired(token: String): Boolean {
        return decodeToken(token).expiration.before(Date())
    }

    fun isValidToken(token: String): Boolean {
        return validateToken(token).isSuccess
    }

    private fun validateToken(token: String): Result<Unit> {
        return runCatching {
            Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
        }
    }

    private fun getSignKey(): SecretKey {
        return Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(jwtProperty.secretKey)
        )
    }
}
