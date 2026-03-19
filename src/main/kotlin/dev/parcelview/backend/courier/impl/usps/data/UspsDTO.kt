package dev.parcelview.backend.courier.impl.usps.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UspsDTO : ArrayList<UspsDTO.UspsDTOItem>(){
    @Serializable
    data class UspsDTOItem(
        val deliveryDateExpectation: DeliveryDateExpectation?,
        val kahalaIndicator: Boolean,
        val mailClass: String,
        val mailClassCode: String,
        val mailType: String,
        val mailingDate: String,
        val serviceTypeCode: String,
        val services: List<String>,
        val servicesEligibility: ServicesEligibility,
        val status: String,
        val statusCategory: String,
        val statusSummary: String,
        val trackingEvents: List<TrackingEvent>,
        val trackingNumber: String,
        val uniqueTrackingID: String
    ) {
        @Serializable
        data class DeliveryDateExpectation(
            val predictedDeliveryDate: String,
            val predictedDeliveryWindowEndTime: String?,
            val predictedDeliveryWindowStartTime: String?,
            val endOfDay: String?,
        )
    
        @Serializable
        data class ServicesEligibility(
            val proofOfDeliveryEnabled: Boolean,
            @SerialName("RREEnabled")
            val rREEnabled: Boolean?,
            val trackingProofOfDeliveryEnabled: Boolean?
        )
    
        @Serializable
        data class TrackingEvent(
            val eventCity: String?,
            val eventCode: String,
            val eventState: String?,
            val eventTimestamp: String,
            val eventType: String,
            val eventZIPCode: String?,
            @SerialName("GMTOffset")
            val gMTOffset: String?,
            @SerialName("GMTTimestamp")
            val gMTTimestamp: String?
        )
    }
}