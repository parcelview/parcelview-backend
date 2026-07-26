package dev.parcelview.status.data

data class StatusResponse(
    val status: String,
    val components: Map<String, String>,
    val info: Map<String, Any?>,
)