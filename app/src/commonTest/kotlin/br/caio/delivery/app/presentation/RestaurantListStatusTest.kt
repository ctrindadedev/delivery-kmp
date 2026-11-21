package br.caio.delivery.app.presentation

import br.caio.delivery.app.data.InMemoryRestaurantRepository
import br.caio.delivery.app.domain.RestaurantRepository
import br.caio.delivery.app.runViewModelTest
import br.caio.delivery.dominio.Restaurant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantListStatusTest {
    @Test
    fun emptyCatalogGoesFromLoadingToEmpty() =
        runViewModelTest {
            val viewModel = RestaurantListViewModel(InMemoryRestaurantRepository(emptyList()))

            assertEquals(RestaurantListStatus.Loading, viewModel.uiState.value.status)
            runCurrent()

            assertEquals(RestaurantListStatus.Empty, viewModel.uiState.value.status)
        }

    @Test
    fun retryLoadsCatalogAfterFailure() =
        runViewModelTest {
            val catalog = InMemoryRestaurantRepository(emptyList())
            val repository = FailsFirstLoad(catalog)
            val viewModel = RestaurantListViewModel(repository)
            runCurrent()
            assertEquals(RestaurantListStatus.Error, viewModel.uiState.value.status)

            catalog.saveRestaurant(null, "Bistrô do Porto")
            viewModel.reload()
            runCurrent()

            assertEquals(RestaurantListStatus.Success, viewModel.uiState.value.status)
            val state = viewModel.uiState.value
            assertEquals("Bistrô do Porto", state.restaurants.single().nome)
        }
}

private class FailsFirstLoad(
    private val delegate: RestaurantRepository,
) : RestaurantRepository by delegate {
    private var firstLoad = true

    override suspend fun listRestaurants(): List<Restaurant> {
        if (firstLoad) {
            firstLoad = false
            error("Falha simulada ao carregar o catálogo")
        }
        return delegate.listRestaurants()
    }
}
