package br.caio.delivery.aplicacao

import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.RestaurantId
import br.caio.delivery.infra.persistencia.RepositoriosEmMemoria
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ServicoDeItensTest {
    private val repositorios = RepositoriosEmMemoria()
    private val restaurantes = ServicoDeRestaurantes(repositorios.restaurantes)
    private val itens = ServicoDeItens(repositorios.restaurantes, repositorios.itens)

    @Test
    fun `cria item no cardapio do restaurante`() =
        runBlocking {
            val cantina = restaurantes.criar("Cantina")

            val lasanha = itens.criar(cantina.id, dados("Lasanha", 4500))

            assertEquals(listOf(lasanha), restaurantes.buscar(cantina.id).itens)
        }

    @Test
    fun `recusa item com preco zero pela regra do dominio`() =
        runBlocking {
            val cantina = restaurantes.criar("Cantina")

            assertThrows<RegraDeNegocioViolada> { itens.criar(cantina.id, dados("Água", 0)) }
            assertEquals(0, itens.listar(cantina.id, Pagina()).total)
        }

    @Test
    fun `recusa atualizar item para preco zero`() =
        runBlocking {
            val cantina = restaurantes.criar("Cantina")
            val lasanha = itens.criar(cantina.id, dados("Lasanha", 4500))

            assertThrows<RegraDeNegocioViolada> { itens.atualizar(cantina.id, lasanha.id, dados("Lasanha", 0)) }
            assertEquals(lasanha, itens.buscar(cantina.id, lasanha.id))
        }

    @Test
    fun `remove itens ate o cardapio ficar vazio`() =
        runBlocking {
            val cantina = restaurantes.criar("Cantina")
            val lasanha = itens.criar(cantina.id, dados("Lasanha", 4500))
            val nhoque = itens.criar(cantina.id, dados("Nhoque", 3900))

            itens.remover(cantina.id, lasanha.id)
            assertEquals(listOf(nhoque), restaurantes.buscar(cantina.id).itens)

            itens.remover(cantina.id, nhoque.id)
            assertEquals(emptyList<Any>(), restaurantes.buscar(cantina.id).itens)
        }

    @Test
    fun `operacoes em restaurante ou item inexistente lancam nao encontrado`() =
        runBlocking {
            val cantina = restaurantes.criar("Cantina")
            val inexistente = RestaurantId.novo()

            assertThrows<RecursoNaoEncontrado> { itens.listar(inexistente, Pagina()) }
            assertThrows<RecursoNaoEncontrado> { itens.criar(inexistente, dados("Lasanha", 4500)) }
            assertThrows<RecursoNaoEncontrado> { itens.buscar(cantina.id, MenuItemId.novo()) }
            assertThrows<RecursoNaoEncontrado> { itens.atualizar(cantina.id, MenuItemId.novo(), dados("X", 100)) }
            assertThrows<RecursoNaoEncontrado> { itens.remover(cantina.id, MenuItemId.novo()) }
            assertThrows<RecursoNaoEncontrado> { itens.remover(inexistente, MenuItemId.novo()) }
            assertThrows<RecursoNaoEncontrado> { itens.buscar(inexistente, MenuItemId.novo()) }
        }

    private fun dados(
        nome: String,
        precoCentavos: Long,
    ) = DadosDoItem(nome, "", Dinheiro(precoCentavos), disponivel = true)
}
