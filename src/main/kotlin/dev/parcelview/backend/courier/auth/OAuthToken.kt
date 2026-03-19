package dev.parcelview.backend.courier.auth

import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

data class OAuthToken(
    val accessToken: String,
    val expiresAt: Instant,
) {
    fun isExpired(bufferSeconds: Long = 30): Boolean =
        Clock.System.now() > expiresAt.minus(bufferSeconds.seconds)
}
