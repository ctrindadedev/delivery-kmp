package br.caio.delivery.app

import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val sampleRestaurants =
    listOf(
        Restaurant(
            id = RestaurantId(Uuid.parse("00000000-0000-0000-0000-000000000001")),
            nome = "Hamburgueria da Vila",
            itens =
                listOf(
                    menuItem("Clássico da casa", "Pão, carne, queijo e molho da casa", 2_590),
                    menuItem("Batata crocante", "Batata com páprica e maionese verde", 1_290),
                ),
        ),
        Restaurant(
            id = RestaurantId(Uuid.parse("00000000-0000-0000-0000-000000000002")),
            nome = "Pizza da Nonna",
            itens =
                listOf(
                    menuItem("Margherita", "Molho de tomate, muçarela e manjericão", 3_490),
                    menuItem("Calabresa", "Calabresa, cebola roxa e muçarela", 3_790),
                ),
        ),
        Restaurant(
            id = RestaurantId(Uuid.parse("00000000-0000-0000-0000-000000000003")),
            nome = "Sabor Potiguar",
            itens =
                listOf(
                    menuItem("Carne de sol", "Carne de sol, macaxeira e queijo coalho", 4_290),
                    menuItem("Cuscuz recheado", "Cuscuz, frango e queijo coalho", 1_890),
                ),
        ),
    )

private fun menuItem(
    name: String,
    description: String,
    priceInCents: Long,
) = MenuItem(
    id = MenuItemId.novo(),
    nome = name,
    descricao = description,
    preco = Dinheiro(priceInCents),
)
