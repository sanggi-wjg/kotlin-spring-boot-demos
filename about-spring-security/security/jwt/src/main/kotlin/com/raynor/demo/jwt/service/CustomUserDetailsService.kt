package com.raynor.demo.jwt.service

import com.raynor.demo.jwt.enum.UserRole
import com.raynor.demo.jwt.model.AuthorizedUser
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
}

@Service
@Transactional(readOnly = true)
class BasicCustomUserDetailsService(
    private val userRepository: UserRepository,
) : CustomUserDetailsService {

    override fun loadUserByUsername(username: String): CustomUserDetails {
        throw IllegalAccessException("❌ You can't touch this. ❌")
    }

    override fun loadUserByEmail(email: String): CustomUserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User $email not found")

        return AuthorizedUser(
            userId = user.id!!,
            username = user.name,
            userEmail = user.email,
            authorities = determinedAuthorities(user)
        )
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
