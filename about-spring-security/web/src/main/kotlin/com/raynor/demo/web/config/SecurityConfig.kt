package com.raynor.demo.web.config

import com.raynor.demo.jwt.enum.UserRole
import com.raynor.demo.jwt.filter.JwtTokenFilter
import com.raynor.demo.jwt.filter.RobotFilter
import com.raynor.demo.jwt.jwt.JwtHelper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(proxyTargetClass = true)
class SecurityConfig(
    private val jwtHelper: JwtHelper,
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationEntryPoint: AuthenticationEntryPoint,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .formLogin { it.disable() }
            .logout { it.disable() }
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(authenticationEntryPoint) }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/ping",
                    "/api/v1/accounts/email-signup",
                    "/api/v1/accounts/email-login",
                ).permitAll()

                it.requestMatchers("/robot").hasAnyAuthority(
                    UserRole.ROBOT.name,
                )

                it.anyRequest().hasAnyAuthority(
                    UserRole.ADMIN.name,
                    UserRole.GENERAL.name,
                    UserRole.ANONYMOUS.name,
                )
            }
            .addFilterBefore(RobotFilter(), AuthorizationFilter::class.java)
            .addFilterBefore(JwtTokenFilter(jwtHelper, userDetailsService), AuthorizationFilter::class.java)
            .authenticationManager { it }
            .authenticationProvider(authenticationProvider())
            .anonymous { it.authorities(UserRole.ANONYMOUS.name) }
            .build()
    }

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider {
        return DaoAuthenticationProvider(passwordEncoder).apply {
            this.setUserDetailsService(userDetailsService)
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        return UrlBasedCorsConfigurationSource().apply {
            this.registerCorsConfiguration("/**",
                CorsConfiguration().apply {
                    this.allowedOriginPatterns = listOf("*")
                    this.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    this.allowedHeaders = listOf("*")
                    this.allowCredentials = true
                }
            )
        }
    }
}
