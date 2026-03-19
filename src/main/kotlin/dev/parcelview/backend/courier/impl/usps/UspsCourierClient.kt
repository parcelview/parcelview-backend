package dev.parcelview.backend.courier.impl.usps

import dev.parcelview.backend.courier.Courier
import dev.parcelview.backend.courier.CourierClient
import dev.parcelview.backend.courier.CourierStatus
import dev.parcelview.backend.courier.impl.usps.data.UspsDTO
import dev.parcelview.backend.courier.impl.usps.data.UspsRequestBody
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
class UspsCourierClient(
    private val restClient: RestClient,
    private val tokenProvider: UspsOAuthTokenProvider,
    @Value("\${courier.usps.base-url}") private val baseUrl: String,
) : CourierClient {
    override val courier: Courier
        get() = Courier.USPS

    override suspend fun fetchTracking(trackingNumber: String): TrackingInfo {
        val token = tokenProvider.getToken()

        val response = try {
            restClient.post()
                .uri("$baseUrl/tracking/v3r2/tracking")
                .body(listOf(UspsRequestBody(trackingNumber = trackingNumber)))
                .header("Authorization", "Bearer ${token.accessToken}")
                .retrieve()
                .body<UspsDTO>()
        } catch (_: Exception) {
            throw TrackingException.TrackingNotFoundException(
                trackingNumber = trackingNumber
            )
        } ?: throw TrackingException.TrackingFetchException(
            trackingNumber = trackingNumber,
            cause = IOException("Failed to fetch tracking: $trackingNumber")
        )
        return mapToTrackingInfo(response.first())
    }

    private fun mapToTrackingInfo(response: UspsDTO.UspsDTOItem): TrackingInfo {
        val info = TrackingInfo(
            trackingNumber = response.trackingNumber,
            courier = courier.value,
            status = normaliseStatus(response.statusCategory),
            statusDetail = response.statusSummary,
            estimatedDelivery = Instant.parse(response.trackingEvents.first().eventTimestamp),
            lastLocation = response.trackingEvents.firstOrNull()?.let { formatLocation(it.eventCity, it.eventState) },
            lastUpdated = Clock.System.now(),
        )

        response.trackingEvents.forEach { event ->
            info.events.add(
                TrackingEvent(
                    timestamp = Instant.parse(event.eventTimestamp),
                    status = normaliseStatus(event.eventType),
                    eventCode = event.eventCode,
                    description = event.eventType,
                    location = formatLocation(event.eventCity, event.eventState).orEmpty(),
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

    private fun normaliseStatus(event: String) = when {
        event.contains("delivered", ignoreCase = true) -> CourierStatus.DELIVERED
        event.contains("out for delivery", ignoreCase = true) -> CourierStatus.OUT_FOR_DELIVERY
        event.contains("arrived", ignoreCase = true) ||
                event.contains("departed", ignoreCase = true) ||
                event.contains("in transit", ignoreCase = true) -> CourierStatus.IN_TRANSIT
        event.contains("in possession", ignoreCase = true) -> CourierStatus.PICKED_UP
        else -> CourierStatus.UNKNOWN
    }
}