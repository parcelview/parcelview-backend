package dev.parcelview.backend.courier.impl.fedex

import dev.parcelview.backend.courier.AbstractCourierClient
import dev.parcelview.backend.courier.Courier
import dev.parcelview.backend.courier.CourierMapping.formatLocation
import dev.parcelview.backend.courier.CourierMapping.normaliseStatus
import dev.parcelview.backend.courier.impl.fedex.data.FedexDTO
import dev.parcelview.backend.courier.impl.fedex.data.FedexRequestBody
import dev.parcelview.backend.entity.TrackingEvent
import dev.parcelview.backend.entity.TrackingInfo
import kotlin.time.Clock
import kotlin.time.Instant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class FedexCourierClient(
    private val restClient: RestClient,
    private val tokenProvider: FedexOAuthClientProvider,
    @Value("\${courier.fedex.base-url}") private val baseUrl: String,
) : AbstractCourierClient<FedexDTO>() {
    override val courier: Courier
        get() = Courier.FEDEX

    override suspend fun fetchTrackingInfo(trackingNumber: String): FedexDTO? {
        val token = tokenProvider.getToken()

        return restClient.post()
            .uri("$baseUrl/track/v1/trackingnumbers")
            .body(FedexRequestBody.fromTrackingNumber(trackingNumber))
            .header("Authorization", "Bearer ${token.accessToken}")
            .retrieve()
            .body<FedexDTO>()
    }

    override fun mapResponse(response: FedexDTO): TrackingInfo {
        val result = response.output.completeTrackResults.first()
        val track = result.trackResults.first()

        val info = TrackingInfo(
            trackingNumber = result.trackingNumber,
            courier = courier.value,
            status = normaliseStatus(track.latestStatusDetail.statusByLocale),
            statusDetail = track.latestStatusDetail.ancillaryDetails.first().reasonDescription,
            estimatedDelivery = Instant.parse(track.dateAndTimes.first().dateTime),
            lastLocation = formatLocation(
                track.latestStatusDetail.scanLocation.city,
                track.latestStatusDetail.scanLocation.stateOrProvinceCode,
            ),
            lastUpdated = Clock.System.now(),
        )

        track.scanEvents.forEach { event ->
            info.events.add(
                TrackingEvent(
                    timestamp = Instant.parseOrNull(event.date) ?: Instant.parse(event.date + "Z"),
                    status = normaliseStatus(event.derivedStatus),
                    courier = courier,
                    eventCode = event.eventType,
                    description = event.eventDescription,
                    location = formatLocation(event.scanLocation?.city, event.scanLocation?.stateOrProvinceCode).orEmpty(),
                    trackingInfoId = info.id,
                )
            )
        }
        return info
    }
}