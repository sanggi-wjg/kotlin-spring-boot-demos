package com.raynor.demo.jwt.service

import com.raynor.demo.jwt.enum.UserRole
import com.raynor.demo.jwt.model.AuthorizationUser
import com.raynor.demo.jwt.model.CustomUserDetails
import com.raynor.demo.mysql.entity.UserEntity
import com.raynor.demo.mysql.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface CustomUserDetailsService : UserDetailsService {
    fun loadUserByEmail(email: String): CustomUserDetails
    fun loadUserByToken(token: String): CustomUserDetails
}

@Service
@Transactional(readOnly = true)
class BasicCustomUserDetailsService(
    private val userRepository: UserRepository,
) : CustomUserDetailsService {

    override fun loadUserByUsername(username: String): CustomUserDetails {
//        throw IllegalAccessException("❌ You can't touch this. ❌")
        return loadUserByEmail(username)
    }

    override fun loadUserByEmail(email: String): CustomUserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User $email not found")

        return AuthorizationUser(
            id = user.id!!,
            name = user.name,
            email = user.email,
            password = user.password,
            authorities = determinedAuthorities(user)
        )
    }

    override fun loadUserByToken(token: String): CustomUserDetails {
        TODO("Not yet implemented")
    }

    private fun determinedAuthorities(user: UserEntity): List<SimpleGrantedAuthority> {
        return if (user.isAdmin) {
            listOf(
                SimpleGrantedAuthority(UserRole.ADMIN.name),
            )
        } else {
            listOf(
                SimpleGrantedAuthority(UserRole.GENERAL.name),
            )
        }
    }
}
