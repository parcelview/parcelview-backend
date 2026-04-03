package dev.parcelview.backend.entity

import dev.parcelview.backend.courier.CourierStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

@Entity
@Table(name = "tracking_info")
data class TrackingInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(unique = true)
    val trackingNumber: String,
    val courier: String,
    @Enumerated(EnumType.STRING)
    val status: CourierStatus,
    val statusDetail: String? = null,
    val estimatedDelivery: Instant? = null,
    val lastLocation: String? = null,
    val lastUpdated: Instant = Clock.System.now(),
    @OneToMany(mappedBy = "trackingInfoId", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val events: MutableSet<TrackingEvent> = mutableSetOf()
) {
    override fun hashCode(): Int = id?.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrackingInfo) return false
        return id != null && id == other.id
    }
    override fun toString(): String =
        "TrackingInfo(id=$id, trackingNumber=$trackingNumber, courier=$courier, status=$status)"
}
