package br.caio.delivery.app.data

import br.caio.delivery.app.domain.MenuItemInput
import br.caio.delivery.app.domain.RestaurantRepository
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId

internal class InMemoryRestaurantRepository(
    initialRestaurants: List<Restaurant>,
) : RestaurantRepository {
    private var restaurants = initialRestaurants.toList()

    override suspend fun listRestaurants(): List<Restaurant> = restaurants

    override suspend fun saveRestaurant(
        restaurantId: RestaurantId?,
        name: String,
    ): Restaurant {
        val current = restaurantId?.let { id -> restaurants.first { it.id == id } }
        val saved = current?.copy(nome = name) ?: Restaurant(id = RestaurantId.novo(), nome = name)
        restaurants =
            if (current == null) {
                restaurants + saved
            } else {
                restaurants.map { if (it.id == saved.id) saved else it }
            }
        return saved
    }

    override suspend fun saveMenuItem(
        restaurantId: RestaurantId,
        menuItemId: MenuItemId?,
        item: MenuItemInput,
    ): MenuItem {
        val current = restaurants.first { it.id == restaurantId }
        val saved = MenuItem(menuItemId ?: MenuItemId.novo(), item.name, item.description, item.price, item.available)
        val updatedItems =
            if (menuItemId == null) {
                current.itens + saved
            } else {
                current.itens.map { if (it.id == menuItemId) saved else it }
            }
        restaurants = restaurants.map { if (it.id == restaurantId) current.copy(itens = updatedItems) else it }
        return saved
    }
}
