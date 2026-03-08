package dev.parcelview.backend.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "tracking_events")
data class TrackingEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val timestamp: Instant,
    val status: String,
    val description: String? = null,
    val location: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_info_id")
    @JsonIgnore
    var trackingInfo: TrackingInfo? = null
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
