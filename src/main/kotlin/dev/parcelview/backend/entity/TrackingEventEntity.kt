package dev.parcelview.backend.entity

import java.time.Instant

data class TrackingEvent(
    val timestamp: Instant,
    val status: String,
    val description: String? = null,
    val location: String? = null
)
