package com.raynor.demo.jwt.filter

import com.raynor.demo.jwt.model.EasterEggToken
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class EasterEggFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        if (request.getHeader("x-secret") != "secret") {
            filterChain.doFilter(request, response)
            return
        }

//        response.status = HttpStatus.ACCEPTED.value()
//        response.writer.write("🇰🇷🇰🇷🇰🇷")
//        return

        SecurityContextHolder.setContext(
            SecurityContextHolder.createEmptyContext().apply {
                this.authentication = EasterEggToken()
            }
        )
        filterChain.doFilter(request, response)
    }
}