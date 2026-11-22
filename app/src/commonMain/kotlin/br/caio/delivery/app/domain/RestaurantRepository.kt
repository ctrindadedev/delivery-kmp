package br.caio.delivery.app.domain

import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId

interface RestaurantRepository {
    suspend fun listRestaurants(): List<Restaurant>

    suspend fun saveRestaurant(
        restaurantId: RestaurantId?,
        name: String,
    ): Restaurant

    suspend fun saveMenuItem(
        restaurantId: RestaurantId,
        menuItemId: MenuItemId?,
        item: MenuItemInput,
    ): MenuItem
}

data class MenuItemInput(
    val name: String,
    val description: String,
    val price: Dinheiro,
    val available: Boolean,
)
