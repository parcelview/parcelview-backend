package dev.parcelview.backend.courier.impl.ontrac

import dev.parcelview.backend.courier.Courier
import dev.parcelview.backend.courier.CourierClient
import dev.parcelview.backend.courier.CourierFetchException
import dev.parcelview.backend.courier.impl.ontrac.data.OnTracDTO
import dev.parcelview.backend.entity.TrackingEvent
import dev.parcelview.backend.entity.TrackingInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.Instant

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
        } catch (e: Exception) {
            throw CourierFetchException(
                courier = courier.value,
                trackingNumber = trackingNumber,
                message = "OnTrac API call failed: ${e.message}",
                cause = e
            )
        } ?: throw CourierFetchException(
            courier = courier.value,
            trackingNumber = trackingNumber,
            message = "OnTrac returned an empty response"
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
            lastUpdated = Instant.now()
        )

        val events = response.events.map { event ->
            TrackingEvent(
                timestamp = Instant.parse(event.utcEventDateTime),
                status = normaliseStatus(event.eventShortDescription),
                description = event.eventLongDescription,
                location = formatLocation(event.city, event.state).orEmpty()
            )
        }

        return info.copy(events = events)
    }

    private fun formatLocation(city: String?, state: String?): String? =
        listOfNotNull(city, state)
            .joinToString(", ")
            .ifBlank { null }

    private fun normaliseStatus(raw: String): String = when (raw.uppercase()) {
        "DELIVERED", "PACKAGE DELIVERED" -> "DELIVERED"
        "OUT_FOR_DELIVERY", "OUT FOR DELIVERY" -> "OUT_FOR_DELIVERY"
        "IN_TRANSIT", "IN TRANSIT" -> "IN_TRANSIT"
        "PICKED_UP", "PICKED UP" -> "PICKED_UP"
        "EXCEPTION" -> "EXCEPTION"
        else -> "UNKNOWN"
    }
}