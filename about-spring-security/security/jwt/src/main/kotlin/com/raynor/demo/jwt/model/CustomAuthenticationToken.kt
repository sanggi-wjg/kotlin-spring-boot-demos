package com.raynor.demo.jwt.model

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class CustomAuthenticationToken(
    val token: String,
    val user: CustomUserDetails,
    authorities: Collection<GrantedAuthority>,
) : AbstractAuthenticationToken(authorities) {

    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): Any {
        return user
    }
}
