package com.raynor.demo.jwt.filter

import com.raynor.demo.jwt.jwt.JwtHelper
import com.raynor.demo.jwt.model.AuthorizationRequest
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

class JwtTokenPreAuthenticatedFilter(
    private val jwtHelper: JwtHelper,
) : AbstractPreAuthenticatedProcessingFilter() {

    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest): Any? {
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)?.substringAfter("Bearer ")
            ?: return null

        return if (jwtHelper.isValidToken(token)) {
            AuthorizationRequest(token)
        } else {
            null
        }
    }

    override fun getPreAuthenticatedCredentials(request: HttpServletRequest): Any? {
        return null
    }
}