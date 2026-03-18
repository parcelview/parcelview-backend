package dev.parcelview.backend.service

import dev.parcelview.backend.courier.CourierClient
import dev.parcelview.backend.courier.CourierClientRegistry
import dev.parcelview.backend.courier.CourierStatus
import dev.parcelview.backend.entity.TrackingEvent
import dev.parcelview.backend.entity.TrackingInfo
import dev.parcelview.backend.repository.TrackingInfoRepository
import dev.parcelview.backend.service.exceptions.TrackingException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class TrackingService(
    private val trackingInfoRepository: TrackingInfoRepository,
    private val courierClientRegistry: CourierClientRegistry,
) {

    suspend fun getTracking(trackingNumber: String, courier: String): TrackingInfo = withContext(Dispatchers.IO) {
        val cachedParcel = trackingInfoRepository.findByTrackingNumberAndCourierIgnoreCase(trackingNumber, courier)
        val client = getClient(courier)

        if (cachedParcel != null && (cachedParcel.status == CourierStatus.DELIVERED || cachedParcel.lastUpdated > Clock.System.now()
                .minus(1.hours))
        ) {
            return@withContext cachedParcel.copy(
                events = cachedParcel.events.sortedByDescending { it.timestamp }.toMutableSet()
            )
        }

        val incomingParcel = client.fetchTracking(trackingNumber)
        val parcelToSave = incomingParcel.copy(
            id = cachedParcel?.id, events = mergeEvents(cachedParcel?.events, incomingParcel.events)
        )

        val savedParcel = trackingInfoRepository.save(parcelToSave)
        val eventsWithParentId = savedParcel.events.map { it.copy(trackingInfoId = savedParcel.id) }.toMutableSet()

        return@withContext trackingInfoRepository.save(savedParcel.copy(events = eventsWithParentId))
    }

    private fun getClient(courier: String): CourierClient =
        courierClientRegistry.getCourier(courier) ?: throw TrackingException.CourierNotFoundException(
            courier, courierClientRegistry.supportedCouriers()
        )

    private fun mergeEvents(
        cached: Collection<TrackingEvent>?,
        incoming: Collection<TrackingEvent>?,
    ) = buildSet {
        val existingByKey = cached?.associateBy { it.timestamp } ?: emptyMap()

        incoming?.forEach { event ->
            val cachedEvent = existingByKey[event.timestamp]
            add(event.copy(id = cachedEvent?.id))
        }
    }.toMutableSet()
}
