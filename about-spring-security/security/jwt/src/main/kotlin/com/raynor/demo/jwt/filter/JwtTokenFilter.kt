package com.raynor.demo.jwt.filter

import com.raynor.demo.jwt.jwt.JwtHelper
import com.raynor.demo.jwt.service.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtTokenFilter(
    private val jwtHelper: JwtHelper,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)?.substringAfter("Bearer ")
            ?: return filterChain.doFilter(request, response)

        if (jwtHelper.isValidToken(token)) {
            val user = (userDetailsService as CustomUserDetailsService).loadUserByToken(token)

//            SecurityContextHolder.getContext().authentication = CustomAuthenticationToken(
//                token = token,
//                user = user,
//                authorities = user.authorities,
//            ).apply {
//                this.isAuthenticated = true
//            }
            SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
                user,
                null,
                user.authorities
            ).apply {
                this.details = WebAuthenticationDetailsSource().buildDetails(request)
            }
        }

        filterChain.doFilter(request, response)
    }
}
