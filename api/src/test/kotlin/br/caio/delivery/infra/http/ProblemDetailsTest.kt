package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.Pagina
import br.caio.delivery.aplicacao.RepositorioDeRestaurantes
import br.caio.delivery.infra.persistencia.RepositoriosEmMemoria
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class ProblemDetailsTest {
    @Test
    fun `nome em branco responde problem details com o campo`() =
        testarApi {
            val resposta = client.postJson("/restaurantes", """{"nome":"   "}""")

            val problema = resposta.problema(HttpStatusCode.BadRequest)
            assertEquals("/problemas/entrada-invalida", problema.texto("type"))
            assertEquals("Entrada inválida", problema.texto("title"))
            assertEquals("/restaurantes", problema.texto("instance"))
            assertEquals(mapOf("nome" to "é obrigatório"), problema.erros())
        }

    @Test
    fun `campos ausentes do item aparecem como obrigatorios`() =
        testarApi {
            val restaurante = client.criarRestaurante("Cantina").id()

            val resposta = client.postJson("/restaurantes/$restaurante/itens", "{}")

            val erros = resposta.problema(HttpStatusCode.BadRequest).erros()
            assertEquals(mapOf("nome" to "é obrigatório", "precoCentavos" to "é obrigatório"), erros)
        }

    @Test
    fun `preco negativo e preco zero sao recusados na borda`() =
        testarApi {
            val restaurante = client.criarRestaurante("Cantina").id()

            listOf(0, -1).forEach { preco ->
                val erros = client.criarItem(restaurante, "Água", preco.toLong()).problema(HttpStatusCode.BadRequest).erros()
                assertEquals(mapOf("precoCentavos" to "deve ser maior que zero"), erros)
            }
        }

    @Test
    fun `atualizar item exige disponivel e criar assume disponivel`() =
        testarApi {
            val restaurante = client.criarRestaurante("Cantina").id()
            val criado = client.criarItem(restaurante, "Lasanha", 4500)
            assertEquals("true", criado.json().texto("disponivel"))

            val semDisponivel =
                client.putJson("/restaurantes/$restaurante/itens/${criado.id()}", """{"nome":"Lasanha","precoCentavos":4500}""")

            assertEquals(mapOf("disponivel" to "é obrigatório"), semDisponivel.problema(HttpStatusCode.BadRequest).erros())
        }

    @Test
    fun `parametros invalidos apontam o parametro`() =
        testarApi {
            assertEquals(
                mapOf("id" to "deve ser um UUID válido"),
                client.get("/restaurantes/abc").problema(HttpStatusCode.BadRequest).erros(),
            )
            assertEquals(
                mapOf("pagina" to "deve ser maior ou igual a 1", "tamanho" to "deve estar entre 1 e 100"),
                client.get("/restaurantes?pagina=0&tamanho=101").problema(HttpStatusCode.BadRequest).erros(),
            )
            assertEquals(
                mapOf("tamanho" to "deve ser um número inteiro"),
                client.get("/restaurantes?tamanho=dez").problema(HttpStatusCode.BadRequest).erros(),
            )
        }

    @Test
    fun `json malformado ou com tipo errado responde corpo invalido`() =
        testarApi {
            val malformado = client.postJson("/restaurantes", """{"nome":""")
            val tipoErrado = client.postJson("/restaurantes", """{"nome":["Cantina"]}""")

            assertEquals("/problemas/corpo-invalido", malformado.problema(HttpStatusCode.BadRequest).texto("type"))
            assertEquals("/problemas/corpo-invalido", tipoErrado.problema(HttpStatusCode.BadRequest).texto("type"))
        }

    @Test
    fun `corpo sem content type json responde 415`() =
        testarApi {
            val resposta =
                client.post("/restaurantes") {
                    contentType(ContentType.Text.Plain)
                    setBody("Cantina")
                }

            resposta.problema(HttpStatusCode.UnsupportedMediaType)
        }

    @Test
    fun `nome e salvo sem espacos nas pontas e repeticao com espacos e conflito`() =
        testarApi {
            val criado = client.criarRestaurante("  Cantina  ")
            assertEquals("Cantina", criado.json().texto("nome"))

            val repetido = client.criarRestaurante(" cantina ")

            assertEquals("/problemas/conflito", repetido.problema(HttpStatusCode.Conflict).texto("type"))
        }

    @Test
    fun `recurso inexistente e rota inexistente respondem 404 em problem details`() =
        testarApi {
            val restaurante = client.get("/restaurantes/00000000-0000-0000-0000-000000000000")
            val rota = client.get("/nao-existe")

            assertEquals("/problemas/recurso-nao-encontrado", restaurante.problema(HttpStatusCode.NotFound).texto("type"))
            assertEquals("/problemas/recurso-nao-encontrado", rota.problema(HttpStatusCode.NotFound).texto("type"))
        }

    @Test
    fun `metodo nao suportado responde 405 em problem details`() =
        testarApi {
            client.patch("/restaurantes").problema(HttpStatusCode.MethodNotAllowed)
        }

    @Test
    fun `falha inesperada responde 500 sem vazar detalhes internos`() {
        val repositorios = RepositoriosEmMemoria()
        val quebrado =
            object : RepositorioDeRestaurantes by repositorios.restaurantes {
                override suspend fun listar(
                    nome: String?,
                    pagina: Pagina,
                ) = error("senha do banco: segredo")
            }

        testarApi(quebrado, repositorios.itens) {
            val resposta = client.get("/restaurantes")

            val problema = resposta.problema(HttpStatusCode.InternalServerError)
            assertEquals("/problemas/erro-interno", problema.texto("type"))
            assertFalse(resposta.bodyAsText().contains("segredo"))
        }
    }

    private suspend fun HttpResponse.problema(esperado: HttpStatusCode): JsonObject {
        assertEquals(esperado, status)
        assertEquals(ContentType.Application.ProblemJson, contentType()?.withoutParameters())
        val problema = json()
        assertEquals(esperado.value, problema["status"]!!.jsonPrimitive.int)
        assertFalse(problema.texto("title").isBlank())
        assertFalse(problema.texto("detail").isBlank())
        return problema
    }

    private fun JsonObject.texto(campo: String) = this[campo]!!.jsonPrimitive.content

    private fun JsonObject.erros() =
        this["erros"]!!.jsonArray.associate {
            it.jsonObject.texto("campo") to it.jsonObject.texto("mensagem")
        }
}
