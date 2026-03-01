package dev.parcelview.backend.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import java.time.Instant

data class TrackingInfo(

    @Id
    val id: Long? = null,
    val trackingNumber: String,
    val courier: String,
    val status: String,
    val statusDetail: String? = null,
    val estimatedDelivery: Instant? = null,
    val lastLocation: String? = null,
    val lastUpdated: Instant = Instant.now(),
    @MappedCollection(idColumn = "TRACKING_INFO_ID", keyColumn = "TRACKING_INFO_KEY")
    val events: List<TrackingEvent> = emptyList()
)
