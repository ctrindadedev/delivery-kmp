package br.caio.delivery.app.data

import br.caio.delivery.app.domain.MenuItemInput
import br.caio.delivery.app.domain.RestaurantRepository
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
internal class RemoteRestaurantRepository(
    private val client: HttpClient,
    baseUrl: String,
) : RestaurantRepository {
    private val restaurantsUrl = "${baseUrl.trimEnd('/')}/restaurantes"

    override suspend fun listRestaurants(): List<Restaurant> =
        pages<RestaurantResponse>(restaurantsUrl).map { restaurant ->
            restaurant.toDomain(pages<MenuItemResponse>("$restaurantsUrl/${restaurant.id}/itens").map { it.toDomain() })
        }

    override suspend fun saveRestaurant(
        restaurantId: RestaurantId?,
        name: String,
    ): Restaurant {
        val url = if (restaurantId == null) restaurantsUrl else "$restaurantsUrl/${restaurantId.value}"
        val method = if (restaurantId == null) HttpMethod.Post else HttpMethod.Put
        return client
            .request(url) {
                this.method = method
                jsonBody(RestaurantRequest(name))
            }.body<RestaurantResponse>()
            .toDomain()
    }

    override suspend fun saveMenuItem(
        restaurantId: RestaurantId,
        menuItemId: MenuItemId?,
        item: MenuItemInput,
    ): MenuItem {
        val itemsUrl = "$restaurantsUrl/${restaurantId.value}/itens"
        val url = if (menuItemId == null) itemsUrl else "$itemsUrl/${menuItemId.value}"
        val method = if (menuItemId == null) HttpMethod.Post else HttpMethod.Put
        return client
            .request(url) {
                this.method = method
                jsonBody(MenuItemRequest(item.name, item.description, item.price.centavos, item.available))
            }.body<MenuItemResponse>()
            .toDomain()
    }

    private suspend inline fun <reified T> pages(url: String): List<T> {
        val items = mutableListOf<T>()
        var pageNumber = 1
        do {
            val page =
                client
                    .get(url) {
                        parameter("pagina", pageNumber)
                        parameter("tamanho", PAGE_SIZE)
                    }.body<PageResponse<T>>()
            if (page.itens.isEmpty()) break
            items.addAll(page.itens)
            pageNumber++
        } while (items.size.toLong() < page.total)
        return items
    }

    private inline fun <reified T : Any> HttpRequestBuilder.jsonBody(value: T) {
        contentType(ContentType.Application.Json)
        setBody(value)
    }
}

private const val PAGE_SIZE = 100
