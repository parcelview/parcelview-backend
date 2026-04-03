package dev.parcelview.backend.controller

import dev.parcelview.backend.courier.CourierClientRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/couriers")
@Tag(name = "Couriers", description = "Discover which courier integrations are currently active")
class CourierController(
    private val registry: CourierClientRegistry
) {

    @GetMapping
    @Operation(
        summary = "List active couriers",
        description = "Returns the set of couriers that are currently configured and available for tracking. " +
                "A courier is only active if its corresponding environment variables were populated at startup " +
                "(e.g. FEDEX_CLIENT_ID and/or FEDEX_CLIENT_SECRET for FedEx). " +
                "Couriers with missing or blank environment variables are skipped and will not appear here.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved active couriers",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = SupportedCouriersResponse::class),
                    examples = [
                        ExampleObject(
                            name = "Multiple couriers active",
                            value = """{"couriers": ["fedex", "usps", "ups"]}"""
                        ),
                        ExampleObject(
                            name = "One courier active",
                            value = """{"couriers": ["fedex"]}"""
                        ),
                        ExampleObject(
                            name = "No couriers active",
                            value = """{"couriers": []}"""
                        ),
                    ]
                )]
            )
        ]
    )
    fun getSupportedCouriers(): SupportedCouriersResponse =
        SupportedCouriersResponse(couriers = registry.supportedCouriers().sorted().toSet())

    @Schema(description = "Response containing the set of active courier integrations")
    data class SupportedCouriersResponse(
        @Schema(
            description = "Lowercase courier identifiers for all currently active integrations",
            example = """["fedex", "ups"]"""
        )
        val couriers: Set<String>
    )
}