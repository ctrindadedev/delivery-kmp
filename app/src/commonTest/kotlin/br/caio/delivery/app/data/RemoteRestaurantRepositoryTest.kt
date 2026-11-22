package br.caio.delivery.app.data

import br.caio.delivery.app.domain.MenuItemInput
import br.caio.delivery.dominio.Dinheiro
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RemoteRestaurantRepositoryTest {
    @Test
    fun readsPaginatedRestaurantsAndTheirMenus() =
        runTest {
            val client =
                mockClient { request ->
                    when (request.url.encodedPath) {
                        "/restaurantes" -> {
                            val page = request.url.parameters["pagina"]
                            val (id, name) = if (page == "1") FIRST_ID to "Bistrô" else SECOND_ID to "Pizzaria"
                            """{"itens":[{"id":"$id","nome":"$name"}],"pagina":$page,"total":2}"""
                        }
                        "/restaurantes/$FIRST_ID/itens" -> """{"itens":[$MENU_ITEM],"total":1}"""
                        "/restaurantes/$SECOND_ID/itens" -> """{"itens":[],"total":0}"""
                        else -> error("Rota inesperada")
                    }
                }
            try {
                val repository = RemoteRestaurantRepository(client, BASE_URL)
                val restaurants = repository.listRestaurants()
                assertEquals(listOf("Bistrô", "Pizzaria"), restaurants.map { it.nome })
                val items = restaurants.first().itens
                assertEquals(Dinheiro(850), items.single().preco)
                assertTrue(restaurants.last().itens.isEmpty())
            } finally {
                client.close()
            }
        }

    @Test
    fun sendsPostForNewRestaurantAndPutForExistingMenuItem() =
        runTest {
            val requests = mutableListOf<HttpRequestData>()
            val client =
                mockClient { request ->
                    requests.add(request)
                    if (request.method == HttpMethod.Post) """{"id":"$FIRST_ID","nome":"Bistrô"}""" else MENU_ITEM
                }
            try {
                val repository = RemoteRestaurantRepository(client, BASE_URL)
                val restaurant = repository.saveRestaurant(null, "Bistrô")
                val existingItem = MenuItemResponse(ITEM_ID, "Suco", "", 850, true).toDomain()
                val input = MenuItemInput("Suco", "", Dinheiro(850), true)
                val saved = repository.saveMenuItem(restaurant.id, existingItem.id, input)

                assertEquals(listOf(HttpMethod.Post, HttpMethod.Put), requests.map { it.method })
                val update = requests.last()
                assertEquals("/restaurantes/$FIRST_ID/itens/$ITEM_ID", update.url.encodedPath)
                val payload = (update.body as TextContent).text
                assertTrue(payload.contains("\"precoCentavos\":850"))
                assertEquals(existingItem.id, saved.id)
            } finally {
                client.close()
            }
        }

    @Test
    fun rejectsServerErrorInsteadOfReturningAnEmptyCatalog() =
        runTest {
            val client =
                HttpClient(MockEngine { respond("{}", HttpStatusCode.InternalServerError, JSON_HEADERS) }) {
                    configureDeliveryHttpClient()
                }
            try {
                val repository = RemoteRestaurantRepository(client, BASE_URL)
                assertFailsWith<ServerResponseException> { repository.listRestaurants() }
            } finally {
                client.close()
            }
        }
}

private fun mockClient(response: (HttpRequestData) -> String): HttpClient =
    HttpClient(MockEngine { request -> respond(response(request), headers = JSON_HEADERS) }) {
        configureDeliveryHttpClient()
    }

private val JSON_HEADERS = headersOf(HttpHeaders.ContentType, "application/json")
private const val BASE_URL = "http://localhost:8080"
private const val FIRST_ID = "00000000-0000-0000-0000-000000000001"
private const val SECOND_ID = "00000000-0000-0000-0000-000000000002"
private const val ITEM_ID = "00000000-0000-0000-0000-000000000003"
private const val MENU_ITEM =
    """{"id":"$ITEM_ID","nome":"Suco","descricao":"","precoCentavos":850,"disponivel":true,"precoFormatado":"R$ 8,50"}"""
