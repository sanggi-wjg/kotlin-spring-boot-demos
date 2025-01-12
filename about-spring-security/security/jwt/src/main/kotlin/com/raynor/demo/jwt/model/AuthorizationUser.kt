package com.raynor.demo.jwt.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

interface CustomUserDetails : UserDetails {
    fun getUserId(): Int
    fun getUserEmail(): String
}

class AuthorizationUser(
    private val id: Int,
    private val name: String,
    private val email: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>,
) : CustomUserDetails {

    override fun getUserId(): Int {
        return id
    }

    override fun getUserEmail(): String {
        return email
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun getUsername(): String {
        return name
    }

    override fun getPassword(): String {
        return password
    }
}