package br.caio.delivery.app.data

import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
internal data class PageResponse<T>(
    val itens: List<T>,
    val total: Long,
)

@Serializable
internal data class RestaurantRequest(
    val nome: String,
)

@Serializable
internal data class RestaurantResponse(
    val id: String,
    val nome: String,
) {
    @OptIn(ExperimentalUuidApi::class)
    fun toDomain(items: List<MenuItem> = emptyList()): Restaurant =
        Restaurant(id = RestaurantId(Uuid.parse(id)), nome = nome, itens = items)
}

@Serializable
internal data class MenuItemRequest(
    val nome: String,
    val descricao: String,
    val precoCentavos: Long,
    val disponivel: Boolean,
)

@Serializable
internal data class MenuItemResponse(
    val id: String,
    val nome: String,
    val descricao: String,
    val precoCentavos: Long,
    val disponivel: Boolean,
) {
    @OptIn(ExperimentalUuidApi::class)
    fun toDomain(): MenuItem =
        MenuItem(
            id = MenuItemId(Uuid.parse(id)),
            nome = nome,
            descricao = descricao,
            preco = Dinheiro(precoCentavos),
            disponivel = disponivel,
        )
}
