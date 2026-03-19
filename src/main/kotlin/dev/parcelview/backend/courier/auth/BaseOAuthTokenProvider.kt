package dev.parcelview.backend.courier.auth

import dev.parcelview.backend.entity.OAuthTokenEntity
import dev.parcelview.backend.repository.OAuthTokenRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseOAuthTokenProvider : OAuthTokenProvider {

    private val log = LoggerFactory.getLogger(javaClass)
    private val mutex = Mutex()
    private var cachedToken: OAuthToken? = null

    @Autowired
    private lateinit var tokenRepository: OAuthTokenRepository

    override suspend fun getToken(): OAuthToken {
        cachedToken?.let { if (!it.isExpired()) return it }

        return mutex.withLock {
            cachedToken?.let { if (!it.isExpired()) return it }

            val persisted = loadFromDatabase()
            if (persisted != null && !persisted.isExpired()) {
                cachedToken = persisted
                return persisted
            }

            log.info("Requesting new OAuth token for {}", courier.value)
            val newToken = requestNewToken()
            saveToDatabase(newToken)
            cachedToken = newToken
            newToken
        }
    }

    protected abstract suspend fun requestNewToken(): OAuthToken


    private fun loadFromDatabase(): OAuthToken? {
        val entity = tokenRepository.findByCourier(courier.value) ?: return null

        if (OAuthToken(entity.accessToken, entity.expiresAt).isExpired()) {
            log.debug("Deleting expired OAuth token for {} from database", courier.value)
            tokenRepository.deleteByCourier(courier.value)
            return null
        }

        log.debug("Loaded valid OAuth token for {} from database", courier.value)
        return OAuthToken(
            accessToken = entity.accessToken,
            expiresAt = entity.expiresAt,
        )
    }

    private fun saveToDatabase(token: OAuthToken) {
        tokenRepository.deleteByCourier(courier.value)
        tokenRepository.save(
            OAuthTokenEntity(
                courier = courier.value,
                accessToken = token.accessToken,
                expiresAt = token.expiresAt,
            )
        )
        log.debug("Persisted OAuth token for {} (expires {})", courier.value, token.expiresAt)
    }
}
