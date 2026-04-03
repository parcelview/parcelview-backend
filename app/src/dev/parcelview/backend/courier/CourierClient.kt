package dev.parcelview.backend.courier

import dev.parcelview.backend.entity.TrackingInfo

/**
 * Contract for a courier-specific tracking integration.
 *
 * Each implementation knows how to call a single courier's API,
 * parse the response, and return a normalised [TrackingInfo].
 */
interface CourierClient {

    val courier: Courier

    /**
     * Fetch live tracking data from the courier's API and map it
     * into the common [TrackingInfo] model.
     *
     * @throws CourierFetchException if the remote call fails
     */
    suspend fun fetchTracking(trackingNumber: String): TrackingInfo
}
