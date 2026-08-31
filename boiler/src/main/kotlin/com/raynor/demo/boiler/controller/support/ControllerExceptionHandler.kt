package com.raynor.demo.boiler.controller.support

import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.MethodNotAllowedException

@ControllerAdvice
class ControllerExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponseDto(
                    status = HttpStatus.BAD_REQUEST.value(),
                    statusText = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = e.message,
                    details = e.bindingResult.fieldErrors.map { error ->
                        ErrorResponseDto.FieldError(
                            field = error.field,
                            message = error.defaultMessage ?: error.rejectedValue.toString(),
                        )
                    },
                ),
            )
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(e: EntityNotFoundException): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponseDto(
                    status = HttpStatus.NOT_FOUND.value(),
                    statusText = HttpStatus.NOT_FOUND.reasonPhrase,
                    message = e.message,
                ),
            )
    }

    @ExceptionHandler(MethodNotAllowedException::class)
    fun handleMethodNotAllowedException(e: MethodNotAllowedException): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(
                ErrorResponseDto(
                    status = HttpStatus.METHOD_NOT_ALLOWED.value(),
                    statusText = HttpStatus.METHOD_NOT_ALLOWED.reasonPhrase,
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponseDto> {
        logger.error("INTERNAL_SERVER_ERROR occurred", e)
        return ResponseEntity
            .internalServerError()
            .body(
                ErrorResponseDto(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    statusText = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                ),
            )
    }
}
