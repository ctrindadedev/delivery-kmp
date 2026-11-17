package br.caio.delivery.app

import androidx.lifecycle.ViewModel
import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Estado da lista, independente de Compose e da forma de obter os restaurantes. */
data class RestaurantListUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val query: String = "",
    val selectedRestaurantId: RestaurantId? = null,
) {
    val visibleRestaurants: List<Restaurant>
        get() {
            val normalizedQuery = query.trim()
            return if (normalizedQuery.isEmpty()) {
                restaurants
            } else {
                restaurants.filter { it.nome.contains(normalizedQuery, ignoreCase = true) }
            }
        }
}

class RestaurantListViewModel(
    initialRestaurants: List<Restaurant> = emptyList(),
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(RestaurantListUiState(restaurants = initialRestaurants.toList()))

    val uiState: StateFlow<RestaurantListUiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.update { current -> current.copy(query = query) }
    }

    fun selectRestaurant(restaurantId: RestaurantId?) {
        _uiState.update { current -> current.copy(selectedRestaurantId = restaurantId) }
    }

    fun updateRestaurants(restaurants: List<Restaurant>) {
        _uiState.update { current -> current.copy(restaurants = restaurants.toList()) }
    }

    fun saveRestaurant(
        restaurantId: RestaurantId?,
        name: String,
    ) {
        _uiState.update { current ->
            current.copy(restaurants = current.restaurants.saveRestaurant(restaurantId, name))
        }
    }

    fun saveMenuItem(
        restaurantId: RestaurantId,
        menuItemId: MenuItemId?,
        draft: MenuItemDraft,
    ) {
        val priceInCents = parsePriceInCents(draft.price) ?: return
        _uiState.update { current ->
            current.copy(restaurants = current.restaurants.saveMenuItem(restaurantId, menuItemId, draft, priceInCents))
        }
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
    draft: MenuItemDraft,
    priceInCents: Long,
): List<Restaurant> =
    map { restaurant ->
        if (restaurant.id != restaurantId) {
            restaurant
        } else {
            val item =
                MenuItem(
                    id = menuItemId ?: MenuItemId.novo(),
                    nome = draft.name.trim(),
                    descricao = draft.description.trim(),
                    preco = Dinheiro(priceInCents),
                    disponivel = draft.available,
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
