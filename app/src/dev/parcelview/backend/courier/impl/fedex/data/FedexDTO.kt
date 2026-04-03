package dev.parcelview.backend.courier.impl.fedex.data


import kotlinx.serialization.Serializable

@Serializable
data class FedexDTO(
    val output: Output,
    val transactionId: String
) {
    @Serializable
    data class Output(
        val completeTrackResults: List<CompleteTrackResult>
    ) {
        @Serializable
        data class CompleteTrackResult(
            val trackResults: List<TrackResult>,
            val trackingNumber: String
        ) {
            @Serializable
            data class TrackResult(
                val additionalTrackingInfo: AdditionalTrackingInfo,
                val availableImages: List<AvailableImage>?,
                val availableNotifications: List<String>,
                val dateAndTimes: List<DateAndTime>,
                val deliveryDetails: DeliveryDetails,
                val destinationLocation: DestinationLocation,
                val estimatedDeliveryTimeWindow: EstimatedDeliveryTimeWindow,
                val goodsClassificationCode: String,
                val lastUpdatedDestinationAddress: LastUpdatedDestinationAddress,
                val latestStatusDetail: LatestStatusDetail,
                val originLocation: OriginLocation,
                val packageDetails: PackageDetails,
                val recipientInformation: RecipientInformation,
                val returnDetail: ReturnDetail?,
                val scanEvents: List<ScanEvent>,
                val serviceDetail: ServiceDetail,
                val shipmentDetails: ShipmentDetails,
                val shipperInformation: ShipperInformation,
                val standardTransitTimeWindow: StandardTransitTimeWindow,
                val trackingNumberInfo: TrackingNumberInfo
            ) {
                @Serializable
                data class AdditionalTrackingInfo(
                    val hasAssociatedShipments: Boolean,
                    val nickname: String,
                    val packageIdentifiers: List<PackageIdentifier>
                ) {
                    @Serializable
                    data class PackageIdentifier(
                        val carrierCode: String,
                        val trackingNumberUniqueId: String,
                        val type: String,
                        val values: List<String>
                    )
                }

                @Serializable
                data class AvailableImage(
                    val size: String,
                    val type: String,
                )

                @Serializable
                data class DateAndTime(
                    val dateTime: String,
                    val type: String
                )

                @Serializable
                data class DeliveryDetails(
                    val actualDeliveryAddress: ActualDeliveryAddress,
                    val deliveryAttempts: String,
                    val deliveryOptionEligibilityDetails: List<DeliveryOptionEligibilityDetail>,
                    val receivedByName: String
                ) {
                    @Serializable
                    data class ActualDeliveryAddress(
                        val city: String,
                        val countryCode: String,
                        val countryName: String,
                        val residential: Boolean,
                        val stateOrProvinceCode: String
                    )

                    @Serializable
                    data class DeliveryOptionEligibilityDetail(
                        val eligibility: String,
                        val option: String
                    )
                }

                @Serializable
                data class DestinationLocation(
                    val locationContactAndAddress: LocationContactAndAddress,
                    val locationType: String
                ) {
                    @Serializable
                    data class LocationContactAndAddress(
                        val address: Address
                    ) {
                        @Serializable
                        data class Address(
                            val city: String,
                            val countryCode: String,
                            val countryName: String,
                            val residential: Boolean,
                            val stateOrProvinceCode: String
                        )
                    }
                }

                @Serializable
                data class EstimatedDeliveryTimeWindow(
                    val window: Window?
                ) {
                    @Serializable
                    class Window
                }

                @Serializable
                data class LastUpdatedDestinationAddress(
                    val city: String,
                    val countryCode: String,
                    val countryName: String,
                    val residential: Boolean,
                    val stateOrProvinceCode: String
                )

                @Serializable
                data class LatestStatusDetail(
                    val ancillaryDetails: List<AncillaryDetail>,
                    val code: String,
                    val derivedCode: String,
                    val description: String,
                    val scanLocation: ScanLocation,
                    val statusByLocale: String
                ) {
                    @Serializable
                    data class AncillaryDetail(
                        val action: String,
                        val actionDescription: String,
                        val reason: String,
                        val reasonDescription: String
                    )

                    @Serializable
                    data class ScanLocation(
                        val city: String,
                        val countryCode: String,
                        val countryName: String,
                        val residential: Boolean,
                        val stateOrProvinceCode: String
                    )
                }

                @Serializable
                data class OriginLocation(
                    val locationContactAndAddress: LocationContactAndAddress,
                    val locationId: String
                ) {
                    @Serializable
                    data class LocationContactAndAddress(
                        val address: Address
                    ) {
                        @Serializable
                        data class Address(
                            val city: String,
                            val countryCode: String,
                            val countryName: String,
                            val residential: Boolean,
                            val stateOrProvinceCode: String
                        )
                    }
                }

                @Serializable
                data class PackageDetails(
                    val count: String,
                    val packageContent: List<String?>,
                    val packagingDescription: PackagingDescription,
                    val physicalPackagingType: String,
                    val sequenceNumber: String,
                    val weightAndDimensions: WeightAndDimensions
                ) {
                    @Serializable
                    data class PackagingDescription(
                        val description: String,
                        val type: String
                    )

                    @Serializable
                    data class WeightAndDimensions(
                        val dimensions: List<Dimension>,
                        val weight: List<Weight>
                    ) {
                        @Serializable
                        data class Dimension(
                            val height: Int,
                            val length: Int,
                            val units: String,
                            val width: Int
                        )

                        @Serializable
                        data class Weight(
                            val unit: String,
                            val value: String
                        )
                    }
                }

                @Serializable
                data class RecipientInformation(
                    val address: Address,
                    val contact: Contact?
                ) {
                    @Serializable
                    data class Address(
                        val city: String,
                        val countryCode: String,
                        val countryName: String,
                        val residential: Boolean,
                        val stateOrProvinceCode: String
                    )

                    @Serializable
                    class Contact
                }

                @Serializable
                class ReturnDetail

                @Serializable
                data class ScanEvent(
                    val date: String,
                    val derivedStatus: String,
                    val derivedStatusCode: String,
                    val eventDescription: String,
                    val eventType: String,
                    val exceptionCode: String,
                    val exceptionDescription: String,
                    val locationId: String? = null,
                    val locationType: String,
                    val scanLocation: ScanLocation? = null,
                ) {
                    @Serializable
                    data class ScanLocation(
                        val city: String? = null,
                        val countryCode: String,
                        val countryName: String,
                        val postalCode: String,
                        val residential: Boolean,
                        val stateOrProvinceCode: String? = null,
                        val streetLines: List<String>
                    )
                }

                @Serializable
                data class ServiceDetail(
                    val description: String,
                    val shortDescription: String,
                    val type: String
                )

                @Serializable
                data class ShipmentDetails(
                    val possessionStatus: Boolean
                )

                @Serializable
                data class ShipperInformation(
                    val address: Address,
                    val contact: Contact?
                ) {
                    @Serializable
                    data class Address(
                        val city: String,
                        val countryCode: String,
                        val countryName: String,
                        val residential: Boolean,
                        val stateOrProvinceCode: String
                    )

                    @Serializable
                    class Contact
                }

                @Serializable
                data class StandardTransitTimeWindow(
                    val window: Window
                ) {
                    @Serializable
                    data class Window(
                        val ends: String
                    )
                }

                @Serializable
                data class TrackingNumberInfo(
                    val carrierCode: String,
                    val trackingNumber: String,
                    val trackingNumberUniqueId: String
                )
            }
        }
    }
}