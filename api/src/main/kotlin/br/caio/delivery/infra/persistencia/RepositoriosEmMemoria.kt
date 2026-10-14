package br.caio.delivery.infra.persistencia

import br.caio.delivery.aplicacao.ConflitoComEstadoAtual
import br.caio.delivery.aplicacao.Pagina
import br.caio.delivery.aplicacao.PaginaDeResultados
import br.caio.delivery.aplicacao.RepositorioDeItens
import br.caio.delivery.aplicacao.RepositorioDeRestaurantes
import br.caio.delivery.aplicacao.restauranteNaoEncontrado
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RepositoriosEmMemoria {
    private val trava = Mutex()
    private val restaurantesPorId = linkedMapOf<RestaurantId, Restaurant>()
    private val itensPorRestaurante = linkedMapOf<RestaurantId, MutableMap<MenuItemId, MenuItem>>()

    val restaurantes: RepositorioDeRestaurantes = Restaurantes()
    val itens: RepositorioDeItens = Itens()

    private inner class Restaurantes : RepositorioDeRestaurantes {
        override suspend fun listar(
            nome: String?,
            pagina: Pagina,
        ) = trava.withLock {
            restaurantesPorId.values
                .filter { nome == null || it.nome.contains(nome, ignoreCase = true) }
                .sortedWith(compareBy({ it.nome.lowercase() }, { it.id.value.toString() }))
                .paginar(pagina)
        }

        override suspend fun buscarPorId(id: RestaurantId) =
            trava.withLock {
                restaurantesPorId[id]?.copy(itens = itensOrdenados(id))
            }

        override suspend fun existe(id: RestaurantId) = trava.withLock { id in restaurantesPorId }

        override suspend fun criar(restaurante: Restaurant) {
            trava.withLock {
                check(restaurante.id !in restaurantesPorId) { "Restaurante ${restaurante.id.value} já existe" }
                garantirNomeUnico(restaurante)
                restaurantesPorId[restaurante.id] = restaurante.copy(itens = emptyList())
                itensPorRestaurante[restaurante.id] = linkedMapOf()
            }
        }

        override suspend fun atualizar(restaurante: Restaurant) =
            trava.withLock {
                val existe = restaurante.id in restaurantesPorId
                if (existe) {
                    garantirNomeUnico(restaurante)
                    restaurantesPorId[restaurante.id] = restaurante.copy(itens = emptyList())
                }
                existe
            }

        override suspend fun remover(id: RestaurantId) =
            trava.withLock {
                itensPorRestaurante.remove(id)
                restaurantesPorId.remove(id) != null
            }
    }

    private inner class Itens : RepositorioDeItens {
        override suspend fun listar(
            restauranteId: RestaurantId,
            pagina: Pagina,
        ) = trava.withLock { itensOrdenados(restauranteId).paginar(pagina) }

        override suspend fun buscarPorId(
            restauranteId: RestaurantId,
            itemId: MenuItemId,
        ) = trava.withLock { itensPorRestaurante[restauranteId]?.get(itemId) }

        override suspend fun criar(
            restauranteId: RestaurantId,
            item: MenuItem,
        ) {
            trava.withLock {
                val itens = itensPorRestaurante[restauranteId] ?: throw restauranteNaoEncontrado(restauranteId)
                itens[item.id] = item
            }
        }

        override suspend fun atualizar(
            restauranteId: RestaurantId,
            item: MenuItem,
        ) = trava.withLock {
            val itens = itensPorRestaurante[restauranteId]
            if (itens == null || item.id !in itens) {
                false
            } else {
                itens[item.id] = item
                true
            }
        }

        override suspend fun remover(
            restauranteId: RestaurantId,
            itemId: MenuItemId,
        ) = trava.withLock { itensPorRestaurante[restauranteId]?.remove(itemId) != null }
    }

    private fun garantirNomeUnico(restaurante: Restaurant) {
        val nomeEmUso =
            restaurantesPorId.values.any {
                it.id != restaurante.id && it.nome.lowercase() == restaurante.nome.lowercase()
            }
        if (nomeEmUso) throw ConflitoComEstadoAtual("Já existe um restaurante com o nome '${restaurante.nome}'")
    }

    private fun itensOrdenados(restauranteId: RestaurantId) =
        itensPorRestaurante[restauranteId]
            .orEmpty()
            .values
            .sortedWith(compareBy({ it.nome.lowercase() }, { it.id.value.toString() }))

    private fun <T> List<T>.paginar(pagina: Pagina) =
        PaginaDeResultados(
            itens = if (pagina.deslocamento >= size) emptyList() else drop(pagina.deslocamento.toInt()).take(pagina.tamanho),
            pagina = pagina,
            total = size.toLong(),
        )
}
