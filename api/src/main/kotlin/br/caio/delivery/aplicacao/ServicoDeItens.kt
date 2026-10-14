package br.caio.delivery.aplicacao

import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId

data class DadosDoItem(
    val nome: String,
    val descricao: String,
    val preco: Dinheiro,
    val disponivel: Boolean,
) {
    fun comId(id: MenuItemId) = MenuItem(id, nome, descricao, preco, disponivel)
}

class ServicoDeItens(
    private val restaurantes: RepositorioDeRestaurantes,
    private val itens: RepositorioDeItens,
) {
    suspend fun listar(
        restauranteId: RestaurantId,
        pagina: Pagina,
    ): PaginaDeResultados<MenuItem> {
        if (!restaurantes.existe(restauranteId)) throw restauranteNaoEncontrado(restauranteId)
        return itens.listar(restauranteId, pagina)
    }

    suspend fun buscar(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ): MenuItem = itens.buscarPorId(restauranteId, itemId) ?: throw naoEncontrado(restauranteId, itemId)

    suspend fun criar(
        restauranteId: RestaurantId,
        dados: DadosDoItem,
    ): MenuItem {
        val restaurante = carregarRestaurante(restauranteId)
        val item = dados.comId(MenuItemId.novo())
        regraDeDominio { restaurante.adicionarItem(item) }
        itens.criar(restauranteId, item)
        return item
    }

    suspend fun atualizar(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
        dados: DadosDoItem,
    ): MenuItem {
        val restaurante = carregarRestaurante(restauranteId)
        val atual = restaurante.itens.find { it.id == itemId } ?: throw itemNaoEncontrado(itemId)
        val atualizado = dados.comId(itemId)
        regraDeDominio { restaurante.removerItem(atual.id).adicionarItem(atualizado) }
        if (!itens.atualizar(restauranteId, atualizado)) throw itemNaoEncontrado(itemId)
        return atualizado
    }

    suspend fun remover(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ) {
        if (!itens.remover(restauranteId, itemId)) throw naoEncontrado(restauranteId, itemId)
    }

    private suspend fun carregarRestaurante(id: RestaurantId): Restaurant =
        restaurantes.buscarPorId(id) ?: throw restauranteNaoEncontrado(id)

    private suspend fun naoEncontrado(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ): RecursoNaoEncontrado = if (restaurantes.existe(restauranteId)) itemNaoEncontrado(itemId) else restauranteNaoEncontrado(restauranteId)

    private fun itemNaoEncontrado(id: MenuItemId) = RecursoNaoEncontrado("Item ${id.value} não encontrado")

    private inline fun regraDeDominio(bloco: () -> Unit) {
        try {
            bloco()
        } catch (violacao: IllegalArgumentException) {
            throw RegraDeNegocioViolada(violacao.message ?: "Regra de negócio violada")
        }
    }
}
