package br.caio.delivery.app

import br.caio.delivery.dominio.MenuItemId

data class RestaurantDetailActions(
    val onBack: (() -> Unit)?,
    val onEditRestaurant: () -> Unit,
    val onCreateMenuItem: () -> Unit,
    val onEditMenuItem: (MenuItemId) -> Unit,
)
