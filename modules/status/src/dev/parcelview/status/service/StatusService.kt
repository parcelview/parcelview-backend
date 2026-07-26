package dev.parcelview.status.service

import dev.parcelview.status.data.StatusResponse
import org.springframework.boot.actuate.info.InfoEndpoint
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint
import org.springframework.boot.health.actuate.endpoint.SystemHealthDescriptor
import org.springframework.stereotype.Service

@Service
class StatusService(
    private val healthEndpoint: HealthEndpoint,
    private val infoEndpoint: InfoEndpoint,
) {
    fun getStatus(): StatusResponse {
        val health = healthEndpoint.health()

        val components = (health as SystemHealthDescriptor)
            .components
            ?.mapValues { (_, component) -> component.status.code }
            ?: emptyMap()

        return StatusResponse(
            status = health.status.code,
            components = components,
            info = infoEndpoint.info(),
        )
    }
}