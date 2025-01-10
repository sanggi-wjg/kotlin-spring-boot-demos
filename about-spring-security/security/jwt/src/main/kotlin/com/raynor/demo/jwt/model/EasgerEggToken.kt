package com.raynor.demo.jwt.model

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils

class EasterEggToken : AbstractAuthenticationToken(
    AuthorityUtils.createAuthorityList("SECRET")
) {
    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): Any {
        return "👍 👍 👍"
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun setAuthenticated(authenticated: Boolean) {
        throw IllegalAccessException("You can't touch this")
    }

    override fun getName(): String {
        return super.getName()
    }
}
