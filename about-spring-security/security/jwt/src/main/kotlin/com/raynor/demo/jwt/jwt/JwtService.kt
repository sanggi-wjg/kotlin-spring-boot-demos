package com.raynor.demo.jwt.jwt

import com.raynor.demo.jwt.AuthorizedUser
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService(
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

    fun isTokenExpired(token: String): Boolean {
        return decodeToken(token).expiration.before(Date())
    }

    fun decodeToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSignKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun getSignKey(): SecretKey {
        return Decoders.BASE64.decode(jwtProperty.secretKey).let {
            Keys.hmacShaKeyFor(it)
        }
    }
}
