package br.caio.delivery.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import br.caio.delivery.app.data.RemoteRestaurantRepository
import br.caio.delivery.app.data.createDeliveryHttpClient
import br.caio.delivery.app.data.deliveryApiUrl
import br.caio.delivery.app.domain.RestaurantRepository
import br.caio.delivery.app.presentation.DeliveryScreen
import br.caio.delivery.app.presentation.RestaurantListViewModel

@Composable
fun DeliveryApp() {
    DeliveryScreen(
        listViewModel =
            viewModel {
                val client = createDeliveryHttpClient()
                createListViewModel(RemoteRestaurantRepository(client, deliveryApiUrl)).also { model ->
                    model.addCloseable(AutoCloseable { client.close() })
                }
            },
    )
}

internal fun createListViewModel(repository: RestaurantRepository) = RestaurantListViewModel(repository)
