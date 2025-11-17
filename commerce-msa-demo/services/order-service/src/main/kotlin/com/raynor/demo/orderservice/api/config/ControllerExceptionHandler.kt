package com.raynor.demo.orderservice.api.config

import com.raynor.demo.productservice.api.config.model.ApiErrorResponse
import com.raynor.demo.productservice.api.config.model.ErrorResponse
import com.raynor.demo.productservice.api.config.model.ValidationErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ControllerExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Request validation failed",
            path = request.requestURI,
            validationErrors = ex.bindingResult.fieldErrors.map {
                ValidationErrorResponse.ValidationError(
                    field = it.field,
                    rejectedValue = it.rejectedValue,
                    message = it.defaultMessage ?: "Invalid value"
                )
            }
        ).let {
            logger.warn(it.toString())

            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(it)
        }
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleException(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message ?: HttpStatus.BAD_REQUEST.reasonPhrase,
            path = request.requestURI,
        ).let {
            logger.warn(it.toString())

            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(it)
        }
    }
}