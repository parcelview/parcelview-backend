package dev.parcelview.backend.service

import dev.parcelview.backend.courier.CourierClientRegistry
import dev.parcelview.backend.courier.CourierFetchException
import dev.parcelview.backend.entity.TrackingInfo
import dev.parcelview.backend.repository.TrackingInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class TrackingService(
    private val trackingInfoRepository: TrackingInfoRepository,
    private val courierClientRegistry: CourierClientRegistry,
) {

    /**
     * Look up tracking info by number and courier.
     * If it doesn't exist locally yet, fetch it from the courier API,
     * normalize it, persist it, and return it.
     */
    suspend fun getTracking(trackingNumber: String, courier: String): TrackingInfo = withContext(Dispatchers.IO) {
        // 1. Check the local cache / DB
        trackingInfoRepository.findByTrackingNumberAndCourier(trackingNumber, courier)?.let { return@withContext it }

        val client = courierClientRegistry.getCourier(courier) ?: throw CourierFetchException(
            courier = courier,
            trackingNumber = trackingNumber,
            message = "Unsupported courier: $courier. " +
                    "Supported: ${courierClientRegistry.supportedCouriers()}"
        )
        val info = client.fetchTracking(trackingNumber)

        return@withContext trackingInfoRepository.save(info)
    }

    suspend fun getAllByCourier(trackingNumber: String): List<TrackingInfo> = withContext(Dispatchers.IO) {
        trackingInfoRepository.findAllByTrackingNumber(trackingNumber)
    }
}
