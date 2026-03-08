package dev.parcelview.backend.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "tracking_info")
data class TrackingInfo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val trackingNumber: String,
    val courier: String,
    val status: String,
    val statusDetail: String? = null,
    val estimatedDelivery: Instant? = null,
    val lastLocation: String? = null,
    val lastUpdated: Instant = Instant.now(),
    @OneToMany(mappedBy = "trackingInfo", cascade = [CascadeType.ALL], orphanRemoval = true)
    val events: MutableSet<TrackingEvent> = mutableSetOf()
) {
    fun addEvent(event: TrackingEvent) {
        events.add(event)
        event.trackingInfo = this
    }
    override fun hashCode(): Int = id?.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrackingInfo) return false
        return id != null && id == other.id
    }
    override fun toString(): String =
        "TrackingInfo(id=$id, trackingNumber=$trackingNumber, courier=$courier, status=$status)"


}
