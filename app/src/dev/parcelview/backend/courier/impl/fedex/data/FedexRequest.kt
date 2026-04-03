package dev.parcelview.backend.courier.impl.fedex.data


data class FedexRequestBody(
    val includeDetailedScans: Boolean = true,
    val trackingInfo: List<TrackingInfo>
) {
    data class TrackingInfo(
        val trackingNumberInfo: TrackingNumberInfo
    ) {
        data class TrackingNumberInfo(
            val trackingNumber: String
        )
    }

    companion object {
        fun fromTrackingNumber(vararg trackingNumber: String, includeDetailedScans: Boolean = true): FedexRequestBody {
            return FedexRequestBody(
                includeDetailedScans = includeDetailedScans,
                trackingInfo = trackingNumber.map {
                    TrackingInfo(
                        trackingNumberInfo = TrackingInfo.TrackingNumberInfo(trackingNumber = it)
                    )
                },
            )
        }
    }
}