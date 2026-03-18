package dev.parcelview.backend.courier.impl.ontrac

import dev.parcelview.backend.courier.Courier
import dev.parcelview.backend.courier.CourierClient
import dev.parcelview.backend.courier.CourierStatus
import dev.parcelview.backend.courier.impl.ontrac.data.OnTracDTO
import dev.parcelview.backend.entity.TrackingEvent
import dev.parcelview.backend.entity.TrackingInfo
import dev.parcelview.backend.service.exceptions.TrackingException
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class OnTracCourierClient(
    private val restClient: RestClient,
    @Value("\${courier.ontrac.base-url}") private val baseUrl: String,
) : CourierClient {
    override val courier: Courier
        get() = Courier.ONTRAC

    override suspend fun fetchTracking(trackingNumber: String): TrackingInfo {
        val response = try {
            restClient.get()
                .uri("$baseUrl/PackageServices/tracking/{trackingNumber}", trackingNumber)
                .retrieve()
                .body<OnTracDTO>()
        } catch (_: Exception) {
            throw TrackingException.TrackingNotFoundException(
                trackingNumber = trackingNumber
            )
        } ?: throw TrackingException.TrackingFetchException(
            trackingNumber = trackingNumber,
            cause = IOException("Failed to fetch tracking: $trackingNumber")
        )
        return mapToTrackingInfo(response.packages.first())
    }

    //TODO: make this more generic to be used with other couriers and move out of this file. this was a tester
    private fun mapToTrackingInfo(response: OnTracDTO.Package): TrackingInfo {
        val info = TrackingInfo(
            trackingNumber = response.tracking,
            courier = courier.value,
            status = normaliseStatus(response.events.first().eventShortDescription),
            statusDetail = response.events.first().eventLongDescription,
            estimatedDelivery = Instant.parse(response.utcExpectedDeliveryDateTime),
            lastLocation = response.events.firstOrNull()?.let { formatLocation(it.city, it.state) },
            lastUpdated = Clock.System.now(),
        )

        response.events.forEach { event ->
            info.events.add(
                TrackingEvent(
                    timestamp = Instant.parse(event.utcEventDateTime),
                    status = normaliseStatus(event.status),
                    description = event.eventLongDescription,
                    eventCode = event.eventCode,
                    location = formatLocation(event.city, event.state).orEmpty(),
                    trackingInfoId = info.id,
                )
            )
        }

        return info
    }

    private fun formatLocation(city: String?, state: String?): String? =
        listOfNotNull(city, state)
            .joinToString(", ")
            .ifBlank { null }

    private fun normaliseStatus(raw: String): CourierStatus = when (raw.uppercase()) {
        "DELIVERED", "PACKAGE DELIVERED" -> CourierStatus.DELIVERED
        "OUT_FOR_DELIVERY", "OUT FOR DELIVERY" -> CourierStatus.OUT_FOR_DELIVERY
        "IN_TRANSIT", "IN TRANSIT" -> CourierStatus.IN_TRANSIT
        "PICKED_UP", "PICKED UP" -> CourierStatus.PICKED_UP
        else -> CourierStatus.UNKNOWN
    }
}