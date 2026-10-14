package br.caio.delivery.infra.http

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RotasDeRestaurantesTest {
    @Test
    fun `cria restaurante com 201 e Location`() =
        testarApi {
            val resposta = client.criarRestaurante("Cantina")

            assertEquals(HttpStatusCode.Created, resposta.status)
            val id = resposta.json()["id"]!!.jsonPrimitive.content
            assertEquals("/restaurantes/$id", resposta.headers[HttpHeaders.Location])
            assertEquals(HttpStatusCode.OK, client.get("/restaurantes/$id").status)
        }

    @Test
    fun `lista com paginacao e filtro por nome`() =
        testarApi {
            listOf("Pizzaria Napoli", "Cantina", "Pizza Express").forEach { client.criarRestaurante(it) }

            val resposta = client.get("/restaurantes?nome=pizza&pagina=1&tamanho=1").json()

            assertEquals(listOf("Pizza Express"), resposta.nomes())
            assertEquals(2, resposta["total"]!!.jsonPrimitive.long)
            assertEquals(1, resposta["tamanho"]!!.jsonPrimitive.long)
        }

    @Test
    fun `tamanho de pagina acima do teto e recusado`() =
        testarApi {
            assertEquals(HttpStatusCode.BadRequest, client.get("/restaurantes?tamanho=101").status)
            assertEquals(HttpStatusCode.BadRequest, client.get("/restaurantes?pagina=abc").status)
        }

    @Test
    fun `atualiza e remove restaurante`() =
        testarApi {
            val id = client.criarRestaurante("Cantina").id()

            val atualizado = client.putJson("/restaurantes/$id", """{"nome":"Cantina da Nonna"}""")
            assertEquals(HttpStatusCode.OK, atualizado.status)
            assertEquals("Cantina da Nonna", atualizado.json()["nome"]!!.jsonPrimitive.content)

            assertEquals(HttpStatusCode.NoContent, client.delete("/restaurantes/$id").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/restaurantes/$id").status)
        }

    @Test
    fun `nome de restaurante repetido responde 409`() =
        testarApi {
            client.criarRestaurante("Cantina")
            val outro = client.criarRestaurante("Pizzaria").id()

            assertEquals(HttpStatusCode.Conflict, client.criarRestaurante("cantina").status)
            assertEquals(HttpStatusCode.Conflict, client.putJson("/restaurantes/$outro", """{"nome":"CANTINA"}""").status)
        }

    @Test
    fun `restaurante inexistente responde 404`() =
        testarApi {
            val inexistente = "00000000-0000-0000-0000-000000000000"

            assertEquals(HttpStatusCode.NotFound, client.get("/restaurantes/$inexistente").status)
            assertEquals(HttpStatusCode.NotFound, client.delete("/restaurantes/$inexistente").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/restaurantes/$inexistente/itens").status)
            assertEquals(
                HttpStatusCode.NotFound,
                client.putJson("/restaurantes/$inexistente", """{"nome":"X"}""").status,
            )
        }

    @Test
    fun `id que nao e UUID responde 400`() =
        testarApi {
            assertEquals(HttpStatusCode.BadRequest, client.get("/restaurantes/abc").status)
        }

    @Test
    fun `gerencia itens pela rota aninhada`() =
        testarApi {
            val restaurante = client.criarRestaurante("Cantina").id()

            val criado = client.criarItem(restaurante, "Lasanha", 4500)
            assertEquals(HttpStatusCode.Created, criado.status)
            val item = criado.id()
            assertEquals("/restaurantes/$restaurante/itens/$item", criado.headers[HttpHeaders.Location])
            assertEquals("R$ 45,00", criado.json()["precoFormatado"]!!.jsonPrimitive.content)

            client.criarItem(restaurante, "Nhoque", 3900)
            assertEquals(listOf("Lasanha", "Nhoque"), client.get("/restaurantes/$restaurante/itens").json().nomes())

            val atualizado =
                client.putJson(
                    "/restaurantes/$restaurante/itens/$item",
                    """{"nome":"Lasanha","precoCentavos":5200,"disponivel":false}""",
                )
            assertEquals(HttpStatusCode.OK, atualizado.status)
            assertEquals(5200, atualizado.json()["precoCentavos"]!!.jsonPrimitive.long)

            assertEquals(HttpStatusCode.NoContent, client.delete("/restaurantes/$restaurante/itens/$item").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/restaurantes/$restaurante/itens/$item").status)
        }

    @Test
    fun `remover o ultimo item deixa o cardapio vazio`() =
        testarApi {
            val restaurante = client.criarRestaurante("Cantina").id()
            val item = client.criarItem(restaurante, "Lasanha", 4500).id()

            assertEquals(HttpStatusCode.NoContent, client.delete("/restaurantes/$restaurante/itens/$item").status)
            assertEquals(emptyList<String>(), client.get("/restaurantes/$restaurante/itens").json().nomes())
        }

    @Test
    fun `item com preco zero responde 400`() =
        testarApi {
            val restaurante = client.criarRestaurante("Cantina").id()

            assertEquals(HttpStatusCode.BadRequest, client.criarItem(restaurante, "Água", 0).status)
        }
}
