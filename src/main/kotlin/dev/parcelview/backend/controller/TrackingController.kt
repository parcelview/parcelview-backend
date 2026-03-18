package dev.parcelview.backend.controller

import dev.parcelview.backend.entity.TrackingInfo
import dev.parcelview.backend.service.TrackingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/tracking", produces = ["application/json"])
@Tag(name = "Tracking", description = "Parcel tracking operations")
class TrackingController(
    private val trackingService: TrackingService
) {
    @GetMapping("/{courier}/{trackingNumber}")
    @Operation(summary = "Get tracking info for a parcel from a specific courier")
    suspend fun getTracking(
        @PathVariable courier: String,
        @PathVariable trackingNumber: String
    ): ResponseEntity<TrackingInfo> {
        val info = trackingService.getTracking(trackingNumber, courier)
        return ResponseEntity.ok(info)
    }
}