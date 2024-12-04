package com.raynor.demo.core.handler

import com.raynor.demo.core.exception.InvalidRequestException
import com.raynor.demo.core.exception.dto.ErrorResponseDto
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class ControllerExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(InvalidRequestException::class)
    fun handleException(ex: InvalidRequestException, request: WebRequest): ResponseEntity<ErrorResponseDto> {
        return ErrorResponseDto(
            errors = ex.validationErrors.map {
                ErrorResponseDto.ErrorItemResponseDto(
                    field = it.field,
                    message = it.message
                )
            }
        ).let {
            ResponseEntity.badRequest().body(it)
        }
    }
}
