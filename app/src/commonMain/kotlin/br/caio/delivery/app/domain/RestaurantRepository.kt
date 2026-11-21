package br.caio.delivery.app.domain

import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId

interface RestaurantRepository {
    suspend fun listRestaurants(): List<Restaurant>

    fun saveRestaurant(
        restaurantId: RestaurantId?,
        name: String,
    ): List<Restaurant>

    fun saveMenuItem(
        restaurantId: RestaurantId,
        menuItemId: MenuItemId?,
        item: MenuItemInput,
    ): List<Restaurant>
}

data class MenuItemInput(
    val name: String,
    val description: String,
    val price: Dinheiro,
    val available: Boolean,
)
