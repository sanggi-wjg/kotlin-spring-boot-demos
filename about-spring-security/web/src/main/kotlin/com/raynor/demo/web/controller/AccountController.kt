package com.raynor.demo.web.controller

import com.raynor.demo.web.controller.dto.auth.*
import com.raynor.demo.web.service.AccountService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/accounts")
class AccountController(
    private val accountService: AccountService,
) {

    @PostMapping("/email-signup")
    fun emailSignup(
        @RequestBody emailSignupRequestDto: EmailSignupRequestDto,
    ): ResponseEntity<EmailSignupResponseDto> {
        return accountService.emailSignup(emailSignupRequestDto).let {
            ResponseEntity.created(URI.create("/api/v1/users/me"))
                .body(it.toEmailSignupResponseDto())
        }
    }

    @PostMapping("/email-login")
    fun emailLogin(
        @RequestBody emailLoginRequestDto: EmailLoginRequestDto,
    ): ResponseEntity<EmailLoginResponseDto> {
        return accountService.emailLogin(emailLoginRequestDto).let {
            ResponseEntity.ok(it.toEmailLoginResponseDto())
        }
    }
}
