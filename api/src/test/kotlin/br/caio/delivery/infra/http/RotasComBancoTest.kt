package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.RepositorioDeItens
import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.RestaurantId
import br.caio.delivery.infra.persistencia.BancoDeDados
import br.caio.delivery.infra.persistencia.ConfiguracaoDoBanco
import br.caio.delivery.infra.persistencia.RepositorioExposedDeItens
import br.caio.delivery.infra.persistencia.RepositorioExposedDeRestaurantes
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RotasComBancoTest {
    private lateinit var banco: BancoDeDados

    @BeforeAll
    fun conectar() {
        banco = BancoDeDados.conectar(ConfiguracaoDoBanco(postgres.jdbcUrl, postgres.username, postgres.password))
    }

    @AfterAll
    fun desconectar() {
        banco.close()
    }

    @Test
    fun `indice unico do banco vira 409 e nao 500`() =
        testarApi(RepositorioExposedDeRestaurantes(banco.database), RepositorioExposedDeItens(banco.database)) {
            assertEquals(HttpStatusCode.Created, client.criarRestaurante("Bistrô").status)

            val repetido = client.criarRestaurante("BISTRÔ")

            assertEquals(HttpStatusCode.Conflict, repetido.status)
            assertEquals(ContentType.Application.ProblemJson, repetido.contentType()?.withoutParameters())
            assertEquals("/problemas/conflito", repetido.json()["type"]!!.jsonPrimitive.content)
        }

    @Test
    fun `constraint do banco que escapa do caso de uso vira 409 e nao 500`() {
        val itensReais = RepositorioExposedDeItens(banco.database)
        val itensComBug =
            object : RepositorioDeItens by itensReais {
                override suspend fun criar(
                    restauranteId: RestaurantId,
                    item: MenuItem,
                ) = itensReais.criar(restauranteId, item.copy(preco = Dinheiro(0)))
            }

        testarApi(RepositorioExposedDeRestaurantes(banco.database), itensComBug) {
            val restaurante = client.criarRestaurante("Cantina com bug").id()

            val resposta = client.criarItem(restaurante, "Lasanha", 4500)

            assertEquals(HttpStatusCode.Conflict, resposta.status)
            assertEquals(ContentType.Application.ProblemJson, resposta.contentType()?.withoutParameters())
            assertEquals("/problemas/conflito", resposta.json()["type"]!!.jsonPrimitive.content)
        }
    }

    @Test
    fun `crud de restaurante e cardapio com paginacao no postgres`() =
        testarApi(RepositorioExposedDeRestaurantes(banco.database), RepositorioExposedDeItens(banco.database)) {
            val restaurante = client.criarRestaurante("Trattoria Fluxo").id()
            listOf("Risoto" to 5200L, "Lasanha" to 4500L, "Nhoque" to 3900L).forEach { (nome, preco) ->
                assertEquals(HttpStatusCode.Created, client.criarItem(restaurante, nome, preco).status)
            }

            val primeira = client.get("/restaurantes/$restaurante/itens?pagina=1&tamanho=2").json()
            val segunda = client.get("/restaurantes/$restaurante/itens?pagina=2&tamanho=2").json()
            assertEquals(listOf("Lasanha", "Nhoque"), primeira.nomes())
            assertEquals(listOf("Risoto"), segunda.nomes())
            assertEquals(3, primeira["total"]!!.jsonPrimitive.long)

            val renomeado = client.putJson("/restaurantes/$restaurante", """{"nome":"Trattoria Fluxo Nova"}""")
            assertEquals(HttpStatusCode.OK, renomeado.status)
            val buscado = client.get("/restaurantes/$restaurante").json()
            assertEquals("Trattoria Fluxo Nova", buscado["nome"]!!.jsonPrimitive.content)

            assertEquals(HttpStatusCode.NoContent, client.delete("/restaurantes/$restaurante").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/restaurantes/$restaurante").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/restaurantes/$restaurante/itens").status)
        }

    @Test
    fun `item de outro restaurante nao e encontrado pela rota aninhada no postgres`() =
        testarApi(RepositorioExposedDeRestaurantes(banco.database), RepositorioExposedDeItens(banco.database)) {
            val dono = client.criarRestaurante("Dono do Item").id()
            val outro = client.criarRestaurante("Outro Restaurante").id()
            val item = client.criarItem(dono, "Pizza", 4000).id()

            assertEquals(HttpStatusCode.NotFound, client.get("/restaurantes/$outro/itens/$item").status)
            assertEquals(HttpStatusCode.NotFound, client.delete("/restaurantes/$outro/itens/$item").status)
            assertEquals(HttpStatusCode.OK, client.get("/restaurantes/$dono/itens/$item").status)
        }

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }
}
