package br.caio.delivery.aplicacao

import br.caio.delivery.dominio.RestaurantId
import br.caio.delivery.infra.persistencia.RepositoriosEmMemoria
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ServicoDeRestaurantesTest {
    private val restaurantes = ServicoDeRestaurantes(RepositoriosEmMemoria().restaurantes)

    @Test
    fun `recusa nome em branco mesmo fora da borda http`() =
        runBlocking {
            val erro = assertThrows<DadosInvalidos> { restaurantes.criar("  ") }

            assertEquals(listOf("nome"), erro.erros.map { it.campo })
        }

    @Test
    fun `salva o nome sem espacos nas pontas ao criar e atualizar`() =
        runBlocking {
            val criado = restaurantes.criar("  Cantina ")
            val atualizado = restaurantes.atualizar(criado.id, " Cantina da Nonna  ")

            assertEquals("Cantina", criado.nome)
            assertEquals("Cantina da Nonna", restaurantes.buscar(atualizado.id).nome)
        }

    @Test
    fun `nome em branco na listagem nao filtra`() =
        runBlocking {
            listOf("Cantina", "Bistrô").forEach { restaurantes.criar(it) }

            val pagina = restaurantes.listar("  ", Pagina())

            assertEquals(2, pagina.total)
        }

    @Test
    fun `operacoes em restaurante inexistente lancam nao encontrado`() =
        runBlocking<Unit> {
            val inexistente = RestaurantId.novo()

            assertThrows<RecursoNaoEncontrado> { restaurantes.buscar(inexistente) }
            assertThrows<RecursoNaoEncontrado> { restaurantes.atualizar(inexistente, "Cantina") }
            assertThrows<RecursoNaoEncontrado> { restaurantes.remover(inexistente) }
        }

    @Test
    fun `restaurante removido deixa de ser encontrado`() =
        runBlocking<Unit> {
            val criado = restaurantes.criar("Cantina")

            restaurantes.remover(criado.id)

            assertThrows<RecursoNaoEncontrado> { restaurantes.buscar(criado.id) }
        }
}
