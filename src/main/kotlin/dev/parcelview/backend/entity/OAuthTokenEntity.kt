package dev.parcelview.backend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kotlin.time.Instant
import java.util.UUID

@Entity
@Table(name = "oauth_token")
data class OAuthTokenEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val courier: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val accessToken: String,

    @Column(nullable = false)
    val expiresAt: Instant,
) {
    override fun hashCode(): Int = id?.hashCode() ?: 0
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OAuthTokenEntity) return false
        return id != null && id == other.id
    }
    override fun toString(): String =
        "OAuthTokenEntity(id=$id, courier=$courier, expiresAt=$expiresAt)"
}
