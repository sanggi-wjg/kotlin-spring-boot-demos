package com.raynor.demo.jwt.model

import com.raynor.demo.jwt.enum.UserRole
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils

class RobotToken : AbstractAuthenticationToken(
    AuthorityUtils.createAuthorityList(UserRole.ROBOT.name),
) {
    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): Any {
        return "🤖 I am a Robot! 🤖"
    }

    override fun isAuthenticated(): Boolean {
        return true
    }

    override fun setAuthenticated(authenticated: Boolean) {
        throw IllegalAccessException("❌ You can't touch this. ❌")
    }

    override fun getName(): String {
        return "🤖 Mr.Robot"
    }
}
