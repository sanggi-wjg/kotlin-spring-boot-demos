package com.raynor.demo.jwt.filter

import com.raynor.demo.jwt.model.RobotToken
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class RobotFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        if (request.getHeader("x-robot-password") == "n00b_passw0rd") {
//            response.status = HttpStatus.ACCEPTED.value()
//            response.writer.write("🇰🇷🇰🇷🇰🇷")
//            return

            SecurityContextHolder.setContext(
                SecurityContextHolder.createEmptyContext().apply {
                    this.authentication = RobotToken()
                }
            )
            filterChain.doFilter(request, response)
            return
        }

        filterChain.doFilter(request, response)
    }
}