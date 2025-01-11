package com.raynor.demo.jwt.model

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils

class RobotToken : AbstractAuthenticationToken(
    AuthorityUtils.createAuthorityList("ROBOT")
) {
    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): Any {
        return "🤖 I think you are a robot! 🤖"
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun setAuthenticated(authenticated: Boolean) {
        throw IllegalAccessException("❌ You can't touch this. ❌")
    }

    override fun getName(): String {
        return "Robot Name: Mr.Robot"
    }
}
