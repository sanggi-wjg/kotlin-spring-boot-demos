package com.raynor.demo.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

interface CustomUserDetails : UserDetails {
    fun getUserId(): Int
    fun getUserEmail(): String
}

class AuthorizedUser(
    private val userId: Int,
    private val username: String,
    private val userEmail: String,
    private val authorities: Collection<GrantedAuthority>,
) : CustomUserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getPassword(): String {
        TODO("Not yet implemented")
    }

    override fun getUsername(): String {
        return username
    }

    override fun getUserId(): Int {
        return userId
    }

    override fun getUserEmail(): String {
        return userEmail
    }
}