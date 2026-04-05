package dev.parcelview.status

import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component

@Component("parcelview")
class ParcelViewHealthIndicator : HealthIndicator {

    override fun health(): Health {
        return Health.up()
            .withDetail("service", "ParcelView Backend")
            .build()
    }
}