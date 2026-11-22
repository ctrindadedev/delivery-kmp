package br.caio.delivery.app.data

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect val deliveryEngine: HttpClientEngineFactory<*>

internal expect val deliveryApiUrl: String

internal fun createDeliveryHttpClient(): HttpClient = HttpClient(deliveryEngine) { configureDeliveryHttpClient() }

internal fun HttpClientConfig<*>.configureDeliveryHttpClient() {
    expectSuccess = true
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
}

private const val REQUEST_TIMEOUT_MILLIS = 10_000L
