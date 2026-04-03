package dev.parcelview.backend.courier

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Resolves courier adapters by name.
 *
 * All [CourierClient] beans are automatically collected by Spring
 * and indexed by their [CourierClient.courier] enum value.
 */
@Component
class CourierClientRegistry(
    @Autowired(required = false)
    courierClients: List<CourierClient>? = null,
) {

    private val registry: Map<String, CourierClient> =
        courierClients.orEmpty().associateBy { it.courier.value.lowercase() }

    /** Returns the registry entry for [courier], or `null` if unsupported. */
    fun getCourier(courier: String): CourierClient? =
        registry[courier.lowercase()]

    /** All currently registered courier names. */
    fun supportedCouriers(): Set<String> = registry.keys
}
