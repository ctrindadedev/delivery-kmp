package br.caio.delivery.infra.http

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.plugin
import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.RoutingNode
import io.ktor.server.routing.RoutingRoot
import io.ktor.server.routing.getAllRoutes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DocumentacaoOpenApiTest {
    @Test
    fun `toda rota da API aparece na especificacao com o metodo certo`() =
        testarApi {
            lateinit var rotasRegistradas: Set<Pair<String, String>>
            application { rotasRegistradas = plugin(RoutingRoot).getAllRoutes().mapNotNull(::metodoECaminho).toSet() }
            val especificacao = client.especificacao()

            val rotasDocumentadas =
                especificacao["paths"]!!
                    .jsonObject
                    .flatMap { (caminho, operacoes) ->
                        operacoes.jsonObject.keys.map { it.uppercase() to caminho }
                    }.toSet()

            val rotasDaApi = rotasRegistradas.filterNot { (_, caminho) -> caminho.ehDaDocumentacao() }.toSet()
            assertEquals(rotasDaApi, rotasDocumentadas)
            assertEquals(11, rotasDocumentadas.size)
        }

    @Test
    fun `ids de caminho sao uuid e paginacao e inteira`() =
        testarApi {
            val operacao = client.especificacao().operacao("/restaurantes/{id}/itens", "get")

            val parametros = operacao["parameters"]!!.jsonArray.associate { it.jsonObject.texto("name") to it.jsonObject }
            val schemaDoId = parametros["id"]!!["schema"]!!.jsonObject
            assertEquals("string", schemaDoId.texto("type"))
            assertEquals("uuid", schemaDoId.texto("format"))
            assertEquals("integer", parametros["tamanho"]!!["schema"]!!.jsonObject.texto("type"))
            assertEquals("integer", parametros["pagina"]!!["schema"]!!.jsonObject.texto("type"))
        }

    @Test
    fun `respostas de erro sao documentadas como problem details`() =
        testarApi {
            val respostas = client.especificacao().operacao("/restaurantes", "post")["responses"]!!.jsonObject

            assertEquals(setOf("201", "400", "409", "415", "500"), respostas.keys)
            listOf("400", "409", "415", "500").forEach { status ->
                val conteudo = respostas[status]!!.jsonObject["content"]!!.jsonObject
                assertEquals(setOf("application/problem+json"), conteudo.keys)
            }
            assertTrue("Location" in respostas["201"]!!.jsonObject["headers"]!!.jsonObject.keys)
        }

    @Test
    fun `corpos marcam os campos obrigatorios`() =
        testarApi {
            val schemas = client.especificacao()["components"]!!.jsonObject["schemas"]!!.jsonObject

            fun obrigatorios(schema: String) =
                schemas[schema]!!
                    .jsonObject["required"]!!
                    .jsonArray
                    .map { it.jsonPrimitive.content }
                    .toSet()

            assertEquals(setOf("nome"), obrigatorios("RestauranteRequisicao"))
            assertEquals(setOf("nome", "precoCentavos"), obrigatorios("NovoItemRequisicao"))
            assertEquals(setOf("nome", "precoCentavos", "disponivel"), obrigatorios("ItemRequisicao"))
        }

    @Test
    fun `swagger ui abre`() =
        testarApi {
            val resposta = client.get("$CAMINHO_DA_SWAGGER_UI/index.html")

            assertEquals(HttpStatusCode.OK, resposta.status)
            assertTrue(resposta.bodyAsText().contains("swagger", ignoreCase = true))
        }

    private fun String.ehDaDocumentacao() = this == CAMINHO_DA_ESPECIFICACAO || startsWith(CAMINHO_DA_SWAGGER_UI)

    private fun metodoECaminho(rota: RoutingNode): Pair<String, String>? {
        val metodo = (rota.selector as? HttpMethodRouteSelector)?.method ?: return null
        val caminho =
            generateSequence(rota.parent) { it.parent }
                .toList()
                .reversed()
                .map { it.selector.toString() }
                .filter { it.isNotEmpty() && !it.startsWith("(") }
                .joinToString("/", prefix = "/")
                .replace("//", "/")
        return metodo.value to caminho
    }

    private suspend fun io.ktor.client.HttpClient.especificacao(): JsonObject =
        Json.parseToJsonElement(get(CAMINHO_DA_ESPECIFICACAO).bodyAsText()).jsonObject

    private fun JsonObject.operacao(
        caminho: String,
        metodo: String,
    ) = this["paths"]!!.jsonObject[caminho]!!.jsonObject[metodo]!!.jsonObject

    private fun JsonObject.texto(campo: String) = this[campo]!!.jsonPrimitive.content
}
