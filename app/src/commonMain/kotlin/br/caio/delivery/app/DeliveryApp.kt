package br.caio.delivery.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import br.caio.delivery.app.data.InMemoryRestaurantRepository
import br.caio.delivery.app.presentation.DeliveryScreen
import br.caio.delivery.app.presentation.RestaurantListViewModel

@Composable
fun DeliveryApp() {
    DeliveryScreen(listViewModel = viewModel { createRestaurantListViewModel() })
}

internal fun createRestaurantListViewModel(): RestaurantListViewModel {
    val repository = InMemoryRestaurantRepository(sampleRestaurants)
    return RestaurantListViewModel(repository)
}
