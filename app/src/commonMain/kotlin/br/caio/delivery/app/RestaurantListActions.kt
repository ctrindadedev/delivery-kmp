package br.caio.delivery.app

import br.caio.delivery.dominio.RestaurantId

data class RestaurantListActions(
    val onQueryChange: (String) -> Unit,
    val onRestaurantClick: (RestaurantId) -> Unit,
    val onCreateRestaurant: () -> Unit,
)
