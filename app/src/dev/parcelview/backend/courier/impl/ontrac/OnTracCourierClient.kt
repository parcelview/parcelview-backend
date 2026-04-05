package dev.parcelview.backend.courier.impl.ontrac

import dev.parcelview.conditionals.ConditionalOnNonBlankProperties
import dev.parcelview.backend.courier.AbstractCourierClient
import dev.parcelview.backend.courier.Courier
import dev.parcelview.backend.courier.CourierMapping.formatLocation
import dev.parcelview.backend.courier.CourierMapping.normaliseStatus
import dev.parcelview.backend.courier.impl.ontrac.data.OnTracDTO
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
    prefix = "courier.ontrac",
    name = ["base-url"],
)
class OnTracCourierClient(
    private val restClient: RestClient,
    @Value("\${courier.ontrac.base-url}") private val baseUrl: String,
) : AbstractCourierClient<OnTracDTO>() {
    override val courier: Courier
        get() = Courier.ONTRAC

    override suspend fun fetchTrackingInfo(trackingNumber: String): OnTracDTO? =
        restClient.get()
            .uri("$baseUrl/PackageServices/tracking/{trackingNumber}", trackingNumber)
            .retrieve()
            .body<OnTracDTO>()

    override fun mapResponse(dto: OnTracDTO): TrackingInfo {
        val response = dto.packages.first()
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
                    courier = courier,
                    description = event.eventLongDescription,
                    eventCode = event.eventCode,
                    location = formatLocation(event.city, event.state).orEmpty(),
                    trackingInfoId = info.id,
                )
            )
        }

        return info
    }
}