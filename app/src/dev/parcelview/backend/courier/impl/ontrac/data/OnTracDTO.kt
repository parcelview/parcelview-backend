package dev.parcelview.backend.courier.impl.ontrac.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnTracDTO(
    @SerialName("Packages")
    val packages: List<Package>
) {
    @Serializable
    data class Package(
        @SerialName("Attributes")
        val attributes: Attributes?,
        @SerialName("Consignee")
        val consignee: Consignee,
        @SerialName("DimensionUnits")
        val dimensionUnits: String,
        @SerialName("Events")
        val events: List<Event>,
        @SerialName("ExpectedDeliveryDate")
        val expectedDeliveryDate: String,
        @SerialName("Height")
        val height: Int,
        @SerialName("Length")
        val length: Int,
        @SerialName("Origin")
        val origin: Origin,
        @SerialName("PodText")
        val podText: String?,
        @SerialName("Reference1")
        val reference1: String,
        @SerialName("Reference2")
        val reference2: String?,
        @SerialName("Reference3")
        val reference3: String?,
        @SerialName("ServiceCode")
        val serviceCode: String,
        @SerialName("ServiceDescription")
        val serviceDescription: String,
        @SerialName("SignatureImageFormat")
        val signatureImageFormat: String?,
        @SerialName("SignatureImageString")
        val signatureImageString: String?,
        @SerialName("TenderedDate")
        val tenderedDate: String,
        @SerialName("Tracking")
        val tracking: String,
        @SerialName("UtcDeliveryDateTime")
        val utcDeliveryDateTime: String?,
        @SerialName("UtcExpectedDeliveryDateTime")
        val utcExpectedDeliveryDateTime: String,
        @SerialName("UtcOrderPlaced")
        val utcOrderPlaced: String,
        @SerialName("VpodImageUrl")
        val vpodImageUrl: String?,
        @SerialName("Weight")
        val weight: Int,
        @SerialName("WeightUnits")
        val weightUnits: String,
        @SerialName("Width")
        val width: Int
    ) {
        @Serializable
        class Attributes

        @Serializable
        data class Consignee(
            @SerialName("Address1")
            val address1: String?,
            @SerialName("Address2")
            val address2: String?,
            @SerialName("Address3")
            val address3: String?,
            @SerialName("City")
            val city: String,
            @SerialName("Contact")
            val contact: String?,
            @SerialName("Country")
            val country: String,
            @SerialName("Name")
            val name: String?,
            @SerialName("Phone")
            val phone: String?,
            @SerialName("PhoneExt")
            val phoneExt: String?,
            @SerialName("PostalCode")
            val postalCode: String,
            @SerialName("State")
            val state: String
        )

        @Serializable
        data class Event(
            @SerialName("City")
            val city: String?,
            @SerialName("Country")
            val country: String?,
            @SerialName("EventCode")
            val eventCode: String,
            @SerialName("EventLongDescription")
            val eventLongDescription: String,
            @SerialName("EventShortDescription")
            val eventShortDescription: String,
            @SerialName("PostalCode")
            val postalCode: String?,
            @SerialName("State")
            val state: String?,
            @SerialName("Status")
            val status: String,
            @SerialName("TimeZone")
            val timeZone: String,
            @SerialName("UtcEventDateTime")
            val utcEventDateTime: String,
            @SerialName("ZonedEventDateTime")
            val zonedEventDateTime: String
        )

        @Serializable
        data class Origin(
            @SerialName("City")
            val city: String,
            @SerialName("Country")
            val country: String,
            @SerialName("PostalCode")
            val postalCode: String,
            @SerialName("State")
            val state: String
        )
    }
}