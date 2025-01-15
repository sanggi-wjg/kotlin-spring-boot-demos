package com.raynor.demo.controller

import com.raynor.demo.controller.response.ErrorResponseDto
import com.raynor.demo.service.exception.InvalidRequestException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
            code = HttpStatus.BAD_REQUEST.value(),
            message = "Invalid request. Please check your request and try again.",
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
