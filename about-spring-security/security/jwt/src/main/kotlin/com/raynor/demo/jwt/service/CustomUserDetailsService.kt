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

@Service
@Transactional(readOnly = true)
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): CustomUserDetails {
        val user = userRepository.findByName(username)
            ?: throw UsernameNotFoundException("User $username not found")

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
