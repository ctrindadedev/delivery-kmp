package br.caio.delivery.aplicacao

import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.RestaurantId
import br.caio.delivery.dominio.validarItem

data class DadosDoItem(
    val nome: String,
    val descricao: String,
    val preco: Dinheiro,
    val disponivel: Boolean,
) {
    fun comId(id: MenuItemId): MenuItem {
        val item = MenuItem(id, nome.trim(), descricao.trim(), preco, disponivel)
        exigirValidos(validarItem(item.nome, item.descricao, item.preco.centavos))
        return item
    }
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
        val item = dados.comId(MenuItemId.novo())
        if (!restaurantes.existe(restauranteId)) throw restauranteNaoEncontrado(restauranteId)
        itens.criar(restauranteId, item)
        return item
    }

    suspend fun atualizar(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
        dados: DadosDoItem,
    ): MenuItem {
        val item = dados.comId(itemId)
        if (!itens.atualizar(restauranteId, item)) throw naoEncontrado(restauranteId, itemId)
        return item
    }

    suspend fun remover(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ) {
        if (!itens.remover(restauranteId, itemId)) throw naoEncontrado(restauranteId, itemId)
    }

    private suspend fun naoEncontrado(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ): RecursoNaoEncontrado =
        if (restaurantes.existe(restauranteId)) {
            RecursoNaoEncontrado("Item ${itemId.value} não encontrado")
        } else {
            restauranteNaoEncontrado(restauranteId)
        }
}
