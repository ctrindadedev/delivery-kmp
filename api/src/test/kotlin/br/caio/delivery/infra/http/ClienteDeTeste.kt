package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.RepositorioDeItens
import br.caio.delivery.aplicacao.RepositorioDeRestaurantes
import br.caio.delivery.infra.persistencia.RepositoriosEmMemoria
import br.caio.delivery.moduloHttp
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun testarApi(bloco: suspend ApplicationTestBuilder.() -> Unit) {
    val repositorios = RepositoriosEmMemoria()
    testarApi(repositorios.restaurantes, repositorios.itens, bloco)
}

fun testarApi(
    restaurantes: RepositorioDeRestaurantes,
    itens: RepositorioDeItens,
    bloco: suspend ApplicationTestBuilder.() -> Unit,
) = testApplication {
    application { moduloHttp(restaurantes, itens) }
    bloco()
}

suspend fun HttpClient.postJson(
    caminho: String,
    corpo: String,
) = post(caminho) { corpoJson(corpo) }

suspend fun HttpClient.putJson(
    caminho: String,
    corpo: String,
) = put(caminho) { corpoJson(corpo) }

private fun HttpRequestBuilder.corpoJson(corpo: String) {
    contentType(ContentType.Application.Json)
    setBody(corpo)
}

suspend fun HttpClient.criarRestaurante(nome: String) = postJson("/restaurantes", """{"nome":"$nome"}""")

suspend fun HttpClient.criarItem(
    restaurante: String,
    nome: String,
    precoCentavos: Long,
) = postJson("/restaurantes/$restaurante/itens", """{"nome":"$nome","precoCentavos":$precoCentavos}""")

suspend fun HttpResponse.json(): JsonObject = Json.parseToJsonElement(bodyAsText()).jsonObject

suspend fun HttpResponse.id() = json()["id"]!!.jsonPrimitive.content

fun JsonObject.nomes() = this["itens"]!!.jsonArray.map { it.jsonObject["nome"]!!.jsonPrimitive.content }
