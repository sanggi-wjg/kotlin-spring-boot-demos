package com.raynor.demo.jwt.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val message = mapOf(
            "error" to HttpStatus.UNAUTHORIZED.reasonPhrase,
            "message" to authException.message,
            "status" to HttpStatus.UNAUTHORIZED.value(),
            "timestamp" to Instant.now().toEpochMilli(),
            "path" to request.requestURI
        )
        logger.warn(message.toString())

        objectMapper.writeValue(response.outputStream, message)
    }
}
