package com.raynor.demo.batchbulkexcel.api.exception

import com.raynor.demo.batchbulkexcel.api.controller.dto.ErrorResponseDto
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class ApiExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(FileValidationException::class)
    fun handleFileValidation(
        e: FileValidationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        log.warn("파일 검증 실패 [{}]: {}", request.requestURI, e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponseDto(
                status = HttpStatus.BAD_REQUEST,
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = e.message,
                path = request.requestURI,
            ),
        )
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSize(
        e: MaxUploadSizeExceededException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        log.warn("파일 크기 초과 [{}]: max={}, {}", request.requestURI, e.maxUploadSize, e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponseDto(
                status = HttpStatus.BAD_REQUEST,
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = "파일 크기가 허용치를 초과했습니다: ${e.maxUploadSize}, ${e.message}",
                path = request.requestURI,
            ),
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(
        e: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        log.warn("요청 본문 파싱 실패 [{}]: {}", request.requestURI, e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponseDto(
                status = HttpStatus.BAD_REQUEST,
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = "요청 본문을 읽을 수 없습니다: ${e.message}",
                path = request.requestURI,
            ),
        )
    }

    @ExceptionHandler(JobNotFoundException::class)
    fun handleJobNotFound(
        e: JobNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        log.warn("잡 조회 실패 [{}]: {}", request.requestURI, e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponseDto(
                status = HttpStatus.NOT_FOUND,
                error = HttpStatus.NOT_FOUND.reasonPhrase,
                message = e.message,
                path = request.requestURI,
            ),
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        log.error("처리되지 않은 예외 [{}]", request.requestURI, e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponseDto(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                message = "서버 내부 오류가 발생했습니다.",
                path = request.requestURI,
            ),
        )
    }
}
