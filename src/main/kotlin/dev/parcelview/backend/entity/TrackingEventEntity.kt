package dev.parcelview.backend.entity

import dev.parcelview.backend.courier.CourierStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID
import kotlin.time.Instant

@Entity
@Table(name = "tracking_events")
data class TrackingEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    val timestamp: Instant,
    @Enumerated(EnumType.STRING)
    val status: CourierStatus,
    val description: String? = null,
    val location: String? = null,
    val eventCode: String,
    @Column(name = "tracking_info_id")
    var trackingInfoId: UUID? = null
) {
    override fun hashCode(): Int = id?.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrackingEvent) return false
        return id != null && id == other.id
    }

    override fun toString(): String =
        "TrackingEvent(id=$id, timestamp=$timestamp, status=$status)"
}
