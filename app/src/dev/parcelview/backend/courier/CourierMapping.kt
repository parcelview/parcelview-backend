package dev.parcelview.backend.courier

object CourierMapping {
    fun formatLocation(city: String?, state: String?): String? =
        listOfNotNull(city, state)
            .joinToString(", ")
            .ifBlank { null }

    fun normaliseStatus(raw: String): CourierStatus = when {
        raw.contains("delivered", ignoreCase = true) -> CourierStatus.DELIVERED
        raw.contains("out for delivery", ignoreCase = true) -> CourierStatus.OUT_FOR_DELIVERY
        raw.contains("arrived", ignoreCase = true) ||
                raw.contains("departed", ignoreCase = true) ||
                raw.contains("in transit", ignoreCase = true) -> CourierStatus.IN_TRANSIT

        raw.contains("picked up", ignoreCase = true) ||
                raw.contains("in possession", ignoreCase = true) -> CourierStatus.PICKED_UP

        else -> CourierStatus.UNKNOWN
    }
}