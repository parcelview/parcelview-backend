package dev.parcelview.backend.repository

import dev.parcelview.backend.entity.TrackingInfo
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TrackingInfoRepository : CrudRepository<TrackingInfo, Long> {

    fun findByTrackingNumberAndCourier(trackingNumber: String, courier: String): TrackingInfo?

    fun findAllByTrackingNumber(trackingNumber: String): List<TrackingInfo>
}
