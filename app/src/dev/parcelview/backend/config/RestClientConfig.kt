package dev.parcelview.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    @Bean
    fun restClient(): RestClient = RestClient.builder()
        .defaultHeaders { headers ->
            headers["User-Agent"] = "ParcelView Backend"
            headers["Accept"] = "application/json"
        }
        .build()
}