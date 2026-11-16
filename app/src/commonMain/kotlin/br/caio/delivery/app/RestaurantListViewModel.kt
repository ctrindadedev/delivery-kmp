package br.caio.delivery.app

import androidx.lifecycle.ViewModel
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

/** Centraliza estado e eventos da lista. A integração com Compose é o próximo commit. */
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
}
