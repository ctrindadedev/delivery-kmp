package br.caio.delivery.infra.persistencia

import br.caio.delivery.aplicacao.ConflitoComEstadoAtual
import br.caio.delivery.aplicacao.Pagina
import br.caio.delivery.aplicacao.RecursoNaoEncontrado
import br.caio.delivery.aplicacao.RepositorioDeItens
import br.caio.delivery.aplicacao.RepositorioDeRestaurantes
import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

abstract class ContratoDeRepositorios {
    private lateinit var restaurantes: RepositorioDeRestaurantes
    private lateinit var itens: RepositorioDeItens

    protected abstract fun novosRepositorios(): Pair<RepositorioDeRestaurantes, RepositorioDeItens>

    @BeforeEach
    fun prepararRepositorios() {
        val (novosRestaurantes, novosItens) = novosRepositorios()
        restaurantes = novosRestaurantes
        itens = novosItens
    }

    @Test
    fun `cria e busca restaurante com o cardapio`() =
        runBlocking {
            val restaurante = restaurante("Cantina")
            restaurantes.criar(restaurante)
            val lasanha = item("Lasanha", 4500)
            itens.criar(restaurante.id, lasanha)

            assertEquals(restaurante.copy(itens = listOf(lasanha)), restaurantes.buscarPorId(restaurante.id))
            assertTrue(restaurantes.existe(restaurante.id))
        }

    @Test
    fun `busca de restaurante inexistente devolve nulo`() =
        runBlocking {
            assertNull(restaurantes.buscarPorId(RestaurantId.novo()))
            assertFalse(restaurantes.existe(RestaurantId.novo()))
        }

    @Test
    fun `lista restaurantes em ordem de nome com paginacao`() =
        runBlocking {
            listOf("Delta", "Alfa", "Charlie", "Bravo", "Eco").forEach { restaurantes.criar(restaurante(it)) }

            val segundaPagina = restaurantes.listar(nome = null, pagina = Pagina(numero = 2, tamanho = 2))

            assertEquals(listOf("Charlie", "Delta"), segundaPagina.itens.map { it.nome })
            assertEquals(5, segundaPagina.total)
        }

    @Test
    fun `ordena por nome sem diferenciar maiusculas`() =
        runBlocking {
            listOf("banana", "Cereja", "abacaxi", "Damasco").forEach { restaurantes.criar(restaurante(it)) }

            val nomes = restaurantes.listar(nome = null, pagina = Pagina()).itens.map { it.nome }

            assertEquals(listOf("abacaxi", "banana", "Cereja", "Damasco"), nomes)
        }

    @Test
    fun `pagina alem do total vem vazia`() =
        runBlocking {
            restaurantes.criar(restaurante("Cantina"))

            val longe = restaurantes.listar(nome = null, pagina = Pagina(numero = 30_000_000, tamanho = 100))

            assertEquals(emptyList<Restaurant>(), longe.itens)
            assertEquals(1, longe.total)
        }

    @Test
    fun `filtra restaurantes por trecho do nome sem diferenciar maiusculas`() =
        runBlocking {
            listOf("Pizzaria Napoli", "Cantina", "PIZZA Express", "Sushi 100%").forEach { restaurantes.criar(restaurante(it)) }

            val pizzarias = restaurantes.listar(nome = "pizza", pagina = Pagina())
            val porcentagem = restaurantes.listar(nome = "100%", pagina = Pagina())
            val curinga = restaurantes.listar(nome = "%", pagina = Pagina())

            assertEquals(listOf("PIZZA Express", "Pizzaria Napoli"), pizzarias.itens.map { it.nome })
            assertEquals(2, pizzarias.total)
            assertEquals(listOf("Sushi 100%"), porcentagem.itens.map { it.nome })
            assertEquals(listOf("Sushi 100%"), curinga.itens.map { it.nome })
        }

    @Test
    fun `atualiza e remove restaurante existente`() =
        runBlocking {
            val restaurante = restaurante("Cantina")
            restaurantes.criar(restaurante)

            assertTrue(restaurantes.atualizar(restaurante.copy(nome = "Cantina da Nonna")))
            assertEquals("Cantina da Nonna", restaurantes.buscarPorId(restaurante.id)?.nome)
            assertTrue(restaurantes.remover(restaurante.id))
            assertNull(restaurantes.buscarPorId(restaurante.id))
        }

    @Test
    fun `atualizar ou remover restaurante inexistente devolve falso`() =
        runBlocking {
            assertFalse(restaurantes.atualizar(restaurante("Fantasma")))
            assertFalse(restaurantes.remover(RestaurantId.novo()))
        }

    @Test
    fun `recusa criar restaurante com nome ja usado sem diferenciar maiusculas`() =
        runBlocking {
            restaurantes.criar(restaurante("Cantina"))

            assertThrows<ConflitoComEstadoAtual> { restaurantes.criar(restaurante("CANTINA")) }
            assertEquals(1, restaurantes.listar(nome = null, pagina = Pagina()).total)
        }

    @Test
    fun `recusa renomear restaurante para nome de outro`() =
        runBlocking {
            val cantina = restaurante("Cantina")
            val pizzaria = restaurante("Pizzaria")
            restaurantes.criar(cantina)
            restaurantes.criar(pizzaria)

            assertThrows<ConflitoComEstadoAtual> { restaurantes.atualizar(pizzaria.copy(nome = "cantina")) }
            assertEquals("Pizzaria", restaurantes.buscarPorId(pizzaria.id)?.nome)
            assertTrue(restaurantes.atualizar(cantina.copy(nome = "CANTINA")))
        }

    @Test
    fun `remover restaurante remove os itens`() =
        runBlocking {
            val restaurante = restaurante("Cantina")
            restaurantes.criar(restaurante)
            val lasanha = item("Lasanha", 4500)
            itens.criar(restaurante.id, lasanha)

            restaurantes.remover(restaurante.id)

            assertNull(itens.buscarPorId(restaurante.id, lasanha.id))
            assertEquals(0, itens.listar(restaurante.id, Pagina()).total)
        }

    @Test
    fun `lista itens do restaurante em ordem de nome com paginacao`() =
        runBlocking {
            val cantina = restaurante("Cantina")
            val outro = restaurante("Outro")
            restaurantes.criar(cantina)
            restaurantes.criar(outro)
            listOf("Tiramisu", "Lasanha", "Nhoque").forEach { itens.criar(cantina.id, item(it, 3000)) }
            itens.criar(outro.id, item("Pastel", 900))

            val primeiraPagina = itens.listar(cantina.id, Pagina(numero = 1, tamanho = 2))

            assertEquals(listOf("Lasanha", "Nhoque"), primeiraPagina.itens.map { it.nome })
            assertEquals(3, primeiraPagina.total)
        }

    @Test
    fun `atualiza e remove item so dentro do proprio restaurante`() =
        runBlocking {
            val cantina = restaurante("Cantina")
            val outro = restaurante("Outro")
            restaurantes.criar(cantina)
            restaurantes.criar(outro)
            val lasanha = item("Lasanha", 4500)
            itens.criar(cantina.id, lasanha)
            val maisCara = lasanha.copy(preco = Dinheiro(5200), disponivel = false)

            assertFalse(itens.atualizar(outro.id, maisCara))
            assertNull(itens.buscarPorId(outro.id, lasanha.id))
            assertTrue(itens.atualizar(cantina.id, maisCara))
            assertEquals(maisCara, itens.buscarPorId(cantina.id, lasanha.id))
            assertFalse(itens.remover(outro.id, lasanha.id))
            assertTrue(itens.remover(cantina.id, lasanha.id))
            assertNull(itens.buscarPorId(cantina.id, lasanha.id))
        }

    @Test
    fun `criar item em restaurante inexistente lanca nao encontrado`() {
        assertThrows<RecursoNaoEncontrado> {
            runBlocking { itens.criar(RestaurantId.novo(), item("Lasanha", 4500)) }
        }
    }

    @Test
    fun `item sem descricao volta com descricao vazia`() =
        runBlocking {
            val cantina = restaurante("Cantina")
            restaurantes.criar(cantina)
            val semDescricao = item("Pão", 500).copy(descricao = "")
            itens.criar(cantina.id, semDescricao)

            assertEquals(semDescricao, itens.buscarPorId(cantina.id, semDescricao.id))
        }

    private fun restaurante(nome: String) = Restaurant(RestaurantId.novo(), nome)

    private fun item(
        nome: String,
        precoCentavos: Long,
    ) = MenuItem(MenuItemId.novo(), nome, "Descrição de $nome", Dinheiro(precoCentavos))
}
