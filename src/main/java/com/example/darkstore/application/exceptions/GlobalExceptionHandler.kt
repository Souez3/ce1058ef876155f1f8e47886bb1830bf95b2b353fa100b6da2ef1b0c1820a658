package com.example.darkstore.application.exceptions

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    data class ApiError(
        val timestamp: Instant,
        val status: Int,
        val error: String,
        val message: String?,
        val path: String,
        val fieldErrors: Map<String, String>? = null,
    )

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException, request: HttpServletRequest) =
        build(HttpStatus.NOT_FOUND, ex.message, request)

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(ex: NoResourceFoundException, request: HttpServletRequest) =
        build(HttpStatus.NOT_FOUND, "Resource not found", request)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException, request: HttpServletRequest) =
        build(HttpStatus.BAD_REQUEST, ex.message, request)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException, request: HttpServletRequest) =
        build(HttpStatus.BAD_REQUEST, "Malformed request body", request)

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(ex: MissingServletRequestParameterException, request: HttpServletRequest) =
        build(HttpStatus.BAD_REQUEST, "Missing parameter '${ex.parameterName}'", request)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(ex: HttpRequestMethodNotSupportedException, request: HttpServletRequest) =
        build(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", request)

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(ex: IllegalStateException, request: HttpServletRequest) =
        build(HttpStatus.CONFLICT, ex.message, request)

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLock(ex: ObjectOptimisticLockingFailureException, request: HttpServletRequest) =
        build(HttpStatus.CONFLICT, "Resource was modified concurrently, please retry", request)

    @ExceptionHandler(BadCredentialsException::class, UsernameNotFoundException::class, DisabledException::class)
    fun handleAuthFailure(ex: Exception, request: HttpServletRequest) =
        build(HttpStatus.UNAUTHORIZED, "Invalid phone or password", request)

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException, request: HttpServletRequest) =
        build(HttpStatus.FORBIDDEN, "Access denied", request)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ApiError> {
        val fieldErrors = LinkedHashMap<String, String>()
        for (fe in ex.bindingResult.fieldErrors) {
            fieldErrors.putIfAbsent(fe.field, fe.defaultMessage ?: "invalid")
        }
        val body = ApiError(
            Instant.now(), 400, "Bad Request", "Validation failed", request.requestURI, fieldErrors
        )
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException, request: HttpServletRequest) =
        build(HttpStatus.BAD_REQUEST, "Invalid value for parameter '${ex.name}'", request)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception, request: HttpServletRequest): ResponseEntity<ApiError> {
        log.error("Unhandled exception at {}", request.requestURI, ex)
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request)
    }

    private fun build(status: HttpStatus, message: String?, request: HttpServletRequest): ResponseEntity<ApiError> =
        ResponseEntity.status(status).body(
            ApiError(Instant.now(), status.value(), status.reasonPhrase, message, request.requestURI)
        )
}
