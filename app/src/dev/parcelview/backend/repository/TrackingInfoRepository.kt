package dev.parcelview.backend.repository

import dev.parcelview.backend.entity.TrackingInfo
import java.util.UUID
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackingInfoRepository : JpaRepository<TrackingInfo, UUID> {

    @EntityGraph(attributePaths = ["events"])
    fun findByTrackingNumberAndCourierIgnoreCase(trackingNumber: String, courier: String): TrackingInfo?

    fun findAllByTrackingNumber(trackingNumber: String): List<TrackingInfo>
}
