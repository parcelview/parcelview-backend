package dev.parcelview.backend.courier

/**
 * Thrown when a [CourierClient] cannot retrieve tracking data.
 */
class CourierFetchException(
    val courier: String,
    val trackingNumber: String,
    override val message: String = "Failed to fetch tracking for $trackingNumber from $courier",
    override val cause: Throwable? = null
) : RuntimeException(message, cause)
