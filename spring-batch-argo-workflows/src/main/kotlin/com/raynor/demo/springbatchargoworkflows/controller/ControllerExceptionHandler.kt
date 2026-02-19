package com.raynor.demo.springbatchargoworkflows.controller

import com.raynor.demo.springbatchargoworkflows.controller.dto.ErrorResponseDto
import com.raynor.demo.springbatchargoworkflows.exceptions.JobExecutionNotFoundException
import com.raynor.demo.springbatchargoworkflows.exceptions.JobExecutionStillRunningException
import com.raynor.demo.springbatchargoworkflows.exceptions.JobNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.batch.core.job.parameters.InvalidJobParametersException
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException
import org.springframework.batch.core.launch.JobExecutionNotRunningException
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.launch.JobRestartException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class ControllerExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponseDto> {
        logger.error("An unexpected error occurred: ${exception.message}", exception)

        return ErrorResponseDto(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            error = "Internal Server Error",
            message = null,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }

    @ExceptionHandler(JobExecutionAlreadyRunningException::class)
    fun handleJobExecutionAlreadyRunningException(
        exception: JobExecutionAlreadyRunningException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDto> {
        logger.warn(exception.message, exception)

        return ErrorResponseDto(
            status = HttpStatus.CONFLICT,
            error = "Job execution already running",
            message = exception.message,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }

    @ExceptionHandler(JobInstanceAlreadyCompleteException::class)
    fun handleJobInstanceAlreadyCompleteException(
        exception: JobInstanceAlreadyCompleteException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDto> {
        logger.warn(exception.message, exception)

        return ErrorResponseDto(
            status = HttpStatus.CONFLICT,
            error = "Job already completed with same parameters",
            message = exception.message,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }

    @ExceptionHandler(JobRestartException::class)
    fun handleJobRestartException(
        exception: JobRestartException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDto> {
        logger.warn(exception.message, exception)

        return ErrorResponseDto(
            status = HttpStatus.CONFLICT,
            error = "Job restart failed.",
            message = exception.message,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }

    @ExceptionHandler(JobExecutionStillRunningException::class)
    fun handleJobExecutionStillRunningException(
        exception: JobExecutionStillRunningException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDto> {
        logger.warn(exception.message, exception)

        return ErrorResponseDto(
            status = HttpStatus.CONFLICT,
            error = "Job execution still running.",
            message = exception.message,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }

    @ExceptionHandler(JobExecutionNotRunningException::class)
    fun handleJobExecutionNotRunningException(
        exception: JobExecutionNotRunningException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDto> {
        logger.warn(exception.message, exception)

        return ErrorResponseDto(
            status = HttpStatus.CONFLICT,
            error = "Job execution not running.",
            message = exception.message,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }

    @ExceptionHandler(InvalidJobParametersException::class)
    fun handleInvalidJobParametersException(
        exception: InvalidJobParametersException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDto> {
        logger.warn(exception.message, exception)

        return ErrorResponseDto(
            status = HttpStatus.BAD_REQUEST,
            error = "Invalid job parameters",
            message = exception.message,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }

    @ExceptionHandler(JobNotFoundException::class)
    fun handleJobNotFoundException(
        exception: JobNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDto> {
        logger.warn(exception.message, exception)

        return ErrorResponseDto(
            status = HttpStatus.NOT_FOUND,
            error = "Job Not Found",
            message = exception.message,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }

    @ExceptionHandler(JobExecutionNotFoundException::class)
    fun handleJobExecutionNotFoundException(
        exception: JobExecutionNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDto> {
        logger.warn(exception.message, exception)

        return ErrorResponseDto(
            status = HttpStatus.NOT_FOUND,
            error = "Job Execution Not Found",
            message = exception.message,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        exception: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDto> {
        logger.warn(exception.message, exception)

        return ErrorResponseDto(
            status = HttpStatus.BAD_REQUEST,
            error = "Invalid request body",
            message = exception.message,
            path = if (request is ServletWebRequest) request.request.requestURI else "",
        ).let {
            ResponseEntity.status(it.status).body(it)
        }
    }
}
