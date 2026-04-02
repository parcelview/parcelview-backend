package dev.parcelview.backend.courier.impl.fedex

import dev.parcelview.backend.config.condition.ConditionalOnNonBlankProperties
import dev.parcelview.backend.courier.Courier
import dev.parcelview.backend.courier.auth.BaseOAuthTokenProvider
import dev.parcelview.backend.courier.auth.OAuthToken
import dev.parcelview.backend.repository.OAuthTokenRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
@ConditionalOnNonBlankProperties(
    prefix = "courier.fedex.oauth",
    name = ["token-url", "client-id", "client-secret"],
)
class FedexOAuthClientProvider(
    private val restClient: RestClient,
    tokenRepository: OAuthTokenRepository,
    @Value("\${courier.fedex.oauth.token-url}") private val tokenUrl: String,
    @Value("\${courier.fedex.oauth.client-id}") private val clientId: String,
    @Value("\${courier.fedex.oauth.client-secret}") private val clientSecret: String,
) : BaseOAuthTokenProvider(tokenRepository) {
    override val courier: Courier
        get() = Courier.FEDEX

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun requestNewToken(): OAuthToken {
        val responseBody = restClient.post()
            .uri(tokenUrl)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body("grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret")
            .retrieve()
            .body<String>() ?: error("Fedex OAuth token could not be fetched")

        val dto = json.decodeFromString<FedexOAuthResponse>(responseBody)

        return OAuthToken(
            accessToken = dto.accessToken,
            expiresAt = Clock.System.now().plus(dto.expiresIn.seconds),
        )
    }

    @Serializable
    private data class FedexOAuthResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("token_type") val tokenType: String,
        @SerialName("expires_in") val expiresIn: Int,
    )

}