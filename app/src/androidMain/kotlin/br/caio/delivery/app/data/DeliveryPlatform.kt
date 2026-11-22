package br.caio.delivery.app.data

import br.caio.delivery.app.BuildConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual val deliveryEngine: HttpClientEngineFactory<*> = OkHttp

internal actual val deliveryApiUrl: String
    get() = BuildConfig.DELIVERY_API_URL
