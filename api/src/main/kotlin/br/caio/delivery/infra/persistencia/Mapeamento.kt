package br.caio.delivery.infra.persistencia

import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

internal fun RestaurantId.paraColuna() = value.toJavaUuid()

internal fun MenuItemId.paraColuna() = value.toJavaUuid()

internal fun ResultRow.paraRestaurante(itens: List<MenuItem> = emptyList()) =
    Restaurant(
        id = RestaurantId(this[TabelaRestaurante.id].toKotlinUuid()),
        nome = this[TabelaRestaurante.nome],
        itens = itens,
    )

internal fun ResultRow.paraItem() =
    MenuItem(
        id = MenuItemId(this[TabelaItemCardapio.id].toKotlinUuid()),
        nome = this[TabelaItemCardapio.nome],
        descricao = this[TabelaItemCardapio.descricao].orEmpty(),
        preco = Dinheiro(this[TabelaItemCardapio.precoCentavos]),
        disponivel = this[TabelaItemCardapio.disponivel],
    )

internal fun UpdateBuilder<*>.preencher(restaurante: Restaurant) {
    this[TabelaRestaurante.nome] = restaurante.nome
}

internal fun UpdateBuilder<*>.preencher(item: MenuItem) {
    this[TabelaItemCardapio.nome] = item.nome
    this[TabelaItemCardapio.descricao] = item.descricao.ifEmpty { null }
    this[TabelaItemCardapio.precoCentavos] = item.preco.centavos
    this[TabelaItemCardapio.disponivel] = item.disponivel
}

internal fun InsertStatement<*>.inserir(restaurante: Restaurant) {
    this[TabelaRestaurante.id] = restaurante.id.paraColuna()
    preencher(restaurante)
}

internal fun InsertStatement<*>.inserir(
    restauranteId: RestaurantId,
    item: MenuItem,
) {
    this[TabelaItemCardapio.id] = item.id.paraColuna()
    this[TabelaItemCardapio.restauranteId] = restauranteId.paraColuna()
    preencher(item)
}
