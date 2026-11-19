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

    override fun listRestaurants(): List<Restaurant> = restaurants

    override fun saveRestaurant(
        restaurantId: RestaurantId?,
        name: String,
    ): List<Restaurant> {
        restaurants = restaurants.saveRestaurant(restaurantId, name)
        return restaurants
    }

    override fun saveMenuItem(
        restaurantId: RestaurantId,
        menuItemId: MenuItemId?,
        item: MenuItemInput,
    ): List<Restaurant> {
        restaurants = restaurants.saveMenuItem(restaurantId, menuItemId, item)
        return restaurants
    }
}

private fun List<Restaurant>.saveRestaurant(
    restaurantId: RestaurantId?,
    name: String,
): List<Restaurant> =
    if (restaurantId == null) {
        this + Restaurant(id = RestaurantId.novo(), nome = name)
    } else {
        map { restaurant ->
            if (restaurant.id == restaurantId) restaurant.copy(nome = name) else restaurant
        }
    }

private fun List<Restaurant>.saveMenuItem(
    restaurantId: RestaurantId,
    menuItemId: MenuItemId?,
    input: MenuItemInput,
): List<Restaurant> =
    map { restaurant ->
        if (restaurant.id != restaurantId) {
            restaurant
        } else {
            val item =
                MenuItem(
                    id = menuItemId ?: MenuItemId.novo(),
                    nome = input.name,
                    descricao = input.description,
                    preco = input.price,
                    disponivel = input.available,
                )
            val items =
                if (menuItemId == null) {
                    restaurant.itens + item
                } else {
                    restaurant.itens.map { current -> if (current.id == menuItemId) item else current }
                }
            restaurant.copy(itens = items)
        }
    }
