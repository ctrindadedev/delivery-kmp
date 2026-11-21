package br.caio.delivery.app.presentation

import br.caio.delivery.dominio.RestaurantId

data class RestaurantListActions(
    val onQueryChange: (String) -> Unit,
    val onRetry: () -> Unit,
    val onRestaurantClick: (RestaurantId) -> Unit,
    val onCreateRestaurant: () -> Unit,
)
