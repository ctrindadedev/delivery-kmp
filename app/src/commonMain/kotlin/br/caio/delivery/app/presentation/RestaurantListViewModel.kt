package br.caio.delivery.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.caio.delivery.app.domain.MenuItemInput
import br.caio.delivery.app.domain.RestaurantRepository
import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RestaurantListStatus {
    data object Loading : RestaurantListStatus

    data object Empty : RestaurantListStatus

    data object Success : RestaurantListStatus

    data object Error : RestaurantListStatus
}

data class RestaurantListUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val query: String = "",
    val selectedRestaurantId: RestaurantId? = null,
    val status: RestaurantListStatus = RestaurantListStatus.Loading,
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
    private val _uiState = MutableStateFlow(RestaurantListUiState())
    private var loadJob: Job? = null

    val uiState: StateFlow<RestaurantListUiState> = _uiState.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        if (loadJob?.isActive == true) return

        loadJob =
            viewModelScope.launch {
                _uiState.update { it.copy(status = RestaurantListStatus.Loading) }
                try {
                    updateCatalog(repository.listRestaurants())
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _uiState.update { it.copy(status = RestaurantListStatus.Error) }
                }
            }
    }

    fun updateQuery(query: String) {
        _uiState.update { current -> current.copy(query = query) }
    }

    fun selectRestaurant(restaurantId: RestaurantId?) {
        _uiState.update { current -> current.copy(selectedRestaurantId = restaurantId) }
    }

    suspend fun saveRestaurant(
        restaurantId: RestaurantId?,
        name: String,
    ) {
        val saved = repository.saveRestaurant(restaurantId, name)
        _uiState.update { current ->
            val restaurants =
                if (restaurantId == null) {
                    current.restaurants + saved
                } else {
                    current.restaurants.map { if (it.id == restaurantId) saved.copy(itens = it.itens) else it }
                }
            current.copy(restaurants = restaurants, status = RestaurantListStatus.Success)
        }
    }

    suspend fun saveMenuItem(
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
        val saved = repository.saveMenuItem(restaurantId, menuItemId, item)
        _uiState.update { current ->
            current.copy(
                restaurants =
                    current.restaurants.map { restaurant ->
                        if (restaurant.id == restaurantId) {
                            restaurant.copy(itens = restaurant.itens.withSavedItem(saved))
                        } else {
                            restaurant
                        }
                    },
            )
        }
    }

    private fun updateCatalog(restaurants: List<Restaurant>) {
        val status = if (restaurants.isEmpty()) RestaurantListStatus.Empty else RestaurantListStatus.Success
        _uiState.update { it.copy(restaurants = restaurants, status = status) }
    }
}

private fun List<MenuItem>.withSavedItem(saved: MenuItem): List<MenuItem> =
    if (any { it.id == saved.id }) map { if (it.id == saved.id) saved else it } else this + saved
