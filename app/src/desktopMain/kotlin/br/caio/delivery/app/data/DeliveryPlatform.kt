package br.caio.delivery.app.data

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

internal actual val deliveryEngine: HttpClientEngineFactory<*> = CIO

internal actual val deliveryApiUrl: String
    get() = System.getenv("DELIVERY_API_URL")?.takeIf { it.isNotBlank() } ?: "http://localhost:8080"
