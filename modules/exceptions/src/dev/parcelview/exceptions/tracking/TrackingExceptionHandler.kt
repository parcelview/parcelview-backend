package dev.parcelview.exceptions.tracking

import kotlin.time.Clock
import kotlin.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class TrackingExceptionHandler {

    @ExceptionHandler(TrackingException.TrackingNotFoundException::class)
    fun handleNotFound(ex: TrackingException.TrackingNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(ex.message))

    @ExceptionHandler(TrackingException.CourierNotFoundException::class)
    fun handleCourierNotFound(ex: TrackingException.CourierNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(ex.message))

    @ExceptionHandler(TrackingException.TrackingFetchException::class)
    fun handleFetchError(ex: TrackingException.TrackingFetchException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse(ex.message))
}

data class ErrorResponse(val message: String?, val timestamp: Instant = Clock.System.now())