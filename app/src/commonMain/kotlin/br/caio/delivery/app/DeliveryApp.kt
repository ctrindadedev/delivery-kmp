package br.caio.delivery.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeliveryApp() {
    var query by remember { mutableStateOf("") }
    var selectedRestaurant by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box {
                RestaurantListScreen(
                    restaurants = sampleRestaurants,
                    query = query,
                    onQueryChange = { query = it },
                    onRestaurantClick = { selectedId ->
                        selectedRestaurant =
                            sampleRestaurants
                                .firstOrNull { it.id == selectedId }
                                ?.nome
                    },
                )

                selectedRestaurant?.let { restaurantName ->
                    Surface(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(20.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.inverseSurface,
                    ) {
                        Text(
                            text = "Restaurante selecionado: $restaurantName",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
                }
            }
        }
    }
}
