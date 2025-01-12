package com.raynor.demo.jwt.provider

import com.raynor.demo.jwt.model.CustomAuthenticationToken
import com.raynor.demo.jwt.service.CustomUserDetailsService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken


class CustomAuthenticationProvider(
    private val userDetailsService: CustomUserDetailsService,
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication? {
        val principal = authentication as CustomAuthenticationToken
        val user = userDetailsService.loadUserByEmail(
            principal.user.getUserEmail()
        )

        return CustomAuthenticationToken(
            token = principal.token,
            user = user,
            authorities = user.authorities
        ).apply {
            this.isAuthenticated = true
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication.let {
            PreAuthenticatedAuthenticationToken::class.java.isAssignableFrom(authentication)
        }
    }
}