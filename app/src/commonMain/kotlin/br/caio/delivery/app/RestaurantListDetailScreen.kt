package br.caio.delivery.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RestaurantListDetailScreen(
    state: DeliveryUiState,
    actions: DeliveryActions,
    navigationActions: WideNavigationActions,
) {
    val selectedRestaurant = state.restaurants.firstOrNull { it.id == state.selectedRestaurantId }

    Row(modifier = Modifier.fillMaxSize()) {
        RestaurantListScreen(
            restaurants = state.restaurants,
            query = state.query,
            actions =
                RestaurantListActions(
                    onQueryChange = actions.onQueryChange,
                    onRestaurantClick = actions.onRestaurantSelected,
                    onCreateRestaurant = navigationActions.onCreateRestaurant,
                ),
            modifier = Modifier.width(LIST_PANE_WIDTH),
        )
        VerticalDivider()
        if (selectedRestaurant == null) {
            EmptyDetailPane(modifier = Modifier.weight(1f))
        } else {
            RestaurantDetailScreen(
                restaurant = selectedRestaurant,
                actions =
                    RestaurantDetailActions(
                        onBack = null,
                        onEditRestaurant = { navigationActions.onEditRestaurant(selectedRestaurant.id) },
                        onCreateMenuItem = { navigationActions.onCreateMenuItem(selectedRestaurant.id) },
                        onEditMenuItem = { navigationActions.onEditMenuItem(selectedRestaurant.id, it) },
                    ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun EmptyDetailPane(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Selecione um restaurante para consultar o cardápio.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val LIST_PANE_WIDTH = 360.dp
