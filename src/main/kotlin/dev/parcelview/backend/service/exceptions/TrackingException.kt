package dev.parcelview.backend.service.exceptions

sealed class TrackingException(message: String) : RuntimeException(message) {

    data class TrackingNotFoundException(val trackingNumber: String, val e: String? = null) :
        TrackingException("Tracking number $trackingNumber not found\n$e")

    data class CourierNotFoundException(val courier: String, val supportedCouriers: Collection<String>) :
        TrackingException("Courier $courier is not supported. Supported couriers are: ${supportedCouriers.joinToString(", ")}")

    data class TrackingFetchException(val trackingNumber: String, override val cause: Throwable) :
        TrackingException("Failed to fetch tracking info for $trackingNumber")
}