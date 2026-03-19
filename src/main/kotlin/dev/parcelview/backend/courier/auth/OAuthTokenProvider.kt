package dev.parcelview.backend.courier.auth

import dev.parcelview.backend.courier.Courier

interface OAuthTokenProvider {

    val courier: Courier

    suspend fun getToken(): OAuthToken
}
