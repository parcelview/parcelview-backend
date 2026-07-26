package dev.parcelview.backend.courier

import dev.parcelview.backend.entity.TrackingInfo
import dev.parcelview.exceptions.tracking.TrackingException
import java.io.IOException

abstract class AbstractCourierClient<R: Any> : CourierClient {
    protected abstract suspend fun fetchTrackingInfo(trackingNumber: String): R?

    protected abstract fun mapResponse(dto: R): TrackingInfo

    override suspend fun fetchTracking(trackingNumber: String): TrackingInfo {
        val response = try {
            fetchTrackingInfo(trackingNumber)
        } catch (e: Exception) {
            throw TrackingException.TrackingNotFoundException(
                trackingNumber = trackingNumber,
                e = e.cause?.message ?: e.message ?: "Unknown error", // temporary
            )
        } ?: throw TrackingException.TrackingFetchException(
            trackingNumber = trackingNumber,
            cause = IOException("Failed to fetch tracking: $trackingNumber"),
        )
        return mapResponse(response)
    }
}