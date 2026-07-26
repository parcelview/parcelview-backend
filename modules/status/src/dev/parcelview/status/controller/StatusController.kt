package dev.parcelview.status.controller

import dev.parcelview.status.data.StatusResponse
import dev.parcelview.status.service.StatusService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/status")
@Tag(name = "Status", description = "Service health and info facade")
class StatusController(
    private val statusService: StatusService,
) {
    @GetMapping(produces = ["application/json"])
    @Operation(
        summary = "Get service status",
        description = "Returns aggregated health and build info for ParcelView.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Service is healthy",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = StatusResponse::class)
                )]
            ),
            ApiResponse(responseCode = "503", description = "Service is unhealthy"),
        ]
    )
    fun getStatus(): StatusResponse = statusService.getStatus()
}