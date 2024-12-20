package com.raynor.demo.aboutcore.scope

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import java.time.Instant
import java.util.*

@Component
class Tracker(
    val id: String = UUID.randomUUID().toString(),
) {
    @PostConstruct
    fun init() {
        println("[Tracker] Bean created at: ${Instant.now()}")
    }

    @PreDestroy
    fun destroy() {
        println("[Tracker] Bean destroyed at: ${Instant.now()}")
    }
}

@Component
@RequestScope
class UserContext(
    val id: String = UUID.randomUUID().toString(),
    var userAgent: String? = null,
    var contentType: String? = null,
) {
    @PostConstruct
    fun init() {
        println("[UserContext] Bean created at: ${Instant.now()}")
    }

    @PreDestroy
    fun destroy() {
        println("[UserContext] Bean destroyed at: ${Instant.now()}")
    }
}

@Component
class UserContextFilter(
    private val userContext: UserContext,
) : Filter {

    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        (request as HttpServletRequest).also {
            userContext.contentType = it.getHeader("Content-Type")
            userContext.userAgent = it.getHeader("User-Agent")
        }
        chain.doFilter(request, response)
    }
}