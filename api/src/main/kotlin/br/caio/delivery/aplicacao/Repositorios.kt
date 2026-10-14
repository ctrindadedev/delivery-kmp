package br.caio.delivery.aplicacao

import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId

// Portas de persistência. A aplicação declara o que precisa; a infraestrutura implementa.

interface RepositorioDeRestaurantes {
    suspend fun listar(
        nome: String?,
        pagina: Pagina,
    ): PaginaDeResultados<Restaurant>

    suspend fun buscarPorId(id: RestaurantId): Restaurant?

    suspend fun existe(id: RestaurantId): Boolean

    suspend fun criar(restaurante: Restaurant)

    suspend fun atualizar(restaurante: Restaurant): Boolean

    suspend fun remover(id: RestaurantId): Boolean
}

interface RepositorioDeItens {
    suspend fun listar(
        restauranteId: RestaurantId,
        pagina: Pagina,
    ): PaginaDeResultados<MenuItem>

    suspend fun buscarPorId(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ): MenuItem?

    suspend fun criar(
        restauranteId: RestaurantId,
        item: MenuItem,
    )

    suspend fun atualizar(
        restauranteId: RestaurantId,
        item: MenuItem,
    ): Boolean

    suspend fun remover(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ): Boolean
}
