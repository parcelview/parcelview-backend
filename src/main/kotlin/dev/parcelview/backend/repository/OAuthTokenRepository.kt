package dev.parcelview.backend.repository

import dev.parcelview.backend.entity.OAuthTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface OAuthTokenRepository : JpaRepository<OAuthTokenEntity, Long> {

    fun findByCourier(courier: String): OAuthTokenEntity?

    @Modifying
    @Transactional
    fun deleteByCourier(courier: String)
}
