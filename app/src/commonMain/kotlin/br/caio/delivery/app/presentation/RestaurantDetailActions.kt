package br.caio.delivery.app.presentation

import br.caio.delivery.dominio.MenuItemId

data class RestaurantDetailActions(
    val onBack: (() -> Unit)?,
    val onEditRestaurant: () -> Unit,
    val onCreateMenuItem: () -> Unit,
    val onEditMenuItem: (MenuItemId) -> Unit,
)
