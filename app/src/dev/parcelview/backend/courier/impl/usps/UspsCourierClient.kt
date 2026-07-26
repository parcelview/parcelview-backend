package dev.parcelview.backend.courier.impl.usps

import dev.parcelview.conditionals.ConditionalOnNonBlankProperties
import dev.parcelview.backend.courier.AbstractCourierClient
import dev.parcelview.backend.courier.Courier
import dev.parcelview.backend.courier.CourierMapping.formatLocation
import dev.parcelview.backend.courier.CourierMapping.normaliseStatus
import dev.parcelview.backend.courier.impl.usps.data.UspsDTO
import dev.parcelview.backend.courier.impl.usps.data.UspsRequestBody
import dev.parcelview.backend.entity.TrackingEvent
import dev.parcelview.backend.entity.TrackingInfo
import kotlin.time.Clock
import kotlin.time.Instant
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
@ConditionalOnNonBlankProperties(
    prefix = "courier.usps",
    name = ["base-url", "oauth.token-url", "oauth.client-id", "oauth.client-secret"]
)
class UspsCourierClient(
    private val restClient: RestClient,
    private val tokenProvider: UspsOAuthTokenProvider,
    @Value("\${courier.usps.base-url}") private val baseUrl: String,
) : AbstractCourierClient<UspsDTO>() {
    override val courier: Courier
        get() = Courier.USPS

    override suspend fun fetchTrackingInfo(trackingNumber: String): UspsDTO? {
        val token = tokenProvider.getToken()

        return restClient.post()
            .uri("$baseUrl/tracking/v3r2/tracking")
            .body(listOf(UspsRequestBody(trackingNumber = trackingNumber)))
            .header("Authorization", "Bearer ${token.accessToken}")
            .retrieve()
            .body<UspsDTO>()
    }

    override fun mapResponse(dto: UspsDTO): TrackingInfo {
        val response = dto.first()
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
                    courier = courier,
                    eventCode = event.eventCode,
                    description = event.eventType,
                    location = formatLocation(event.eventCity, event.eventState).orEmpty(),
                    trackingInfoId = info.id,
                )
            )
        }

        return info
    }
}