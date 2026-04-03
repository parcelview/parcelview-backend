package dev.parcelview.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Status", description = "Gives status on the service")
class StatusController {

    data class HealthStatus(
        val status: String,
        val uptime: String,
        val message: String,
    )

    private val startTime = System.currentTimeMillis()

    @RequestMapping(
        value = ["/v1/status"],
        produces = ["application/json"],
        method = [RequestMethod.GET]
    )
    @Operation(
        summary = "Get the health status of the service",
        description = "Returns the current health status and uptime of the service.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful response",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HealthStatus::class)
                    )
                ]
            )
        ]
    )
    fun getStatus(): HealthStatus {
        val uptime = System.currentTimeMillis() - startTime
        val uptimeFormatted = formatUptime(uptime)
        return HealthStatus("UP", uptimeFormatted, "Hello ParcelView backend")
    }

    private fun formatUptime(uptimeMillis: Long): String {
        val seconds = (uptimeMillis / 1000) % 60
        val minutes = (uptimeMillis / (1000 * 60)) % 60
        val hours = (uptimeMillis / (1000 * 60 * 60)) % 24
        val days = uptimeMillis / (1000 * 60 * 60 * 24)

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0 || days > 0) append("${hours}h ")
            if (minutes > 0 || hours > 0 || days > 0) append("${minutes}m ")
            append("${seconds}s")
        }.trim()
    }
}