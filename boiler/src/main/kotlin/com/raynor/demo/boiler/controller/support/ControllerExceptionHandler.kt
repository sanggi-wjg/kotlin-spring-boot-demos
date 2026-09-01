package com.raynor.demo.boiler.controller.support

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ControllerExceptionHandler : ResponseEntityExceptionHandler() {
    /** 표준 예외의 기본 변환 */
    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val status = HttpStatus.valueOf(statusCode.value())
        return ResponseEntity
            .status(status)
            .headers(headers)
            .body(
                ErrorResponseDto(
                    status = status.value(),
                    statusText = status.reasonPhrase,
                    message = ex.message,
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

    /** @Valid @RequestBody 검증 실패 - 어떤 필드가 왜 틀렸는지 내려준다. */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponseDto(
                    status = HttpStatus.BAD_REQUEST.value(),
                    statusText = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = "요청 값이 올바르지 않습니다",
                    details = ex.bindingResult.fieldErrors.map { error ->
                        ErrorResponseDto.FieldError(
                            field = error.field,
                            message = error.defaultMessage ?: error.rejectedValue.toString(),
                        )
                    },
                ),
            )
    }

    /** @RequestParam / @PathVariable 등 컨트롤러 파라미터의 제약 위반 (@Min, @Max ...). */
    override fun handleHandlerMethodValidationException(
        ex: HandlerMethodValidationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        return ResponseEntity
            .badRequest()
            .body(
                ErrorResponseDto(
                    status = HttpStatus.BAD_REQUEST.value(),
                    statusText = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = "요청 값이 올바르지 않습니다",
                    details = ex.parameterValidationResults.map { result ->
                        ErrorResponseDto.FieldError(
                            field = result.methodParameter.parameterName ?: result.methodParameter.parameterType.simpleName,
                            message = result.resolvableErrors.firstOrNull()?.defaultMessage ?: "",
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
}
