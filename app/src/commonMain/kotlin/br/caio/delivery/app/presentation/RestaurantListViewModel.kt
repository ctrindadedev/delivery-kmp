package br.caio.delivery.app.presentation

import androidx.lifecycle.ViewModel
import br.caio.delivery.app.domain.MenuItemInput
import br.caio.delivery.app.domain.RestaurantRepository
import br.caio.delivery.dominio.Dinheiro
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
    private val repository: RestaurantRepository,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(RestaurantListUiState(restaurants = repository.listRestaurants()))

    val uiState: StateFlow<RestaurantListUiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.update { current -> current.copy(query = query) }
    }

    fun selectRestaurant(restaurantId: RestaurantId?) {
        _uiState.update { current -> current.copy(selectedRestaurantId = restaurantId) }
    }

    fun saveRestaurant(
        restaurantId: RestaurantId?,
        name: String,
    ) {
        val restaurants = repository.saveRestaurant(restaurantId, name)
        _uiState.update { it.copy(restaurants = restaurants) }
    }

    fun saveMenuItem(
        restaurantId: RestaurantId,
        menuItemId: MenuItemId?,
        draft: MenuItemDraft,
    ) {
        val priceInCents = parsePriceInCents(draft.price) ?: return
        val item =
            MenuItemInput(
                name = draft.name.trim(),
                description = draft.description.trim(),
                price = Dinheiro(priceInCents),
                available = draft.available,
            )
        val restaurants = repository.saveMenuItem(restaurantId, menuItemId, item)
        _uiState.update { it.copy(restaurants = restaurants) }
    }
}
