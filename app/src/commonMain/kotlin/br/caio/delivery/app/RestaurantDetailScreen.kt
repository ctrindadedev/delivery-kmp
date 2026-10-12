package br.caio.delivery.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.caio.delivery.app.theme.DeliveryTheme
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant?,
    actions: RestaurantDetailActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            actions.onBack?.let { onBack ->
                TextButton(onClick = onBack) {
                    Text("Voltar")
                }
            }

            if (restaurant == null) {
                MissingRestaurantMessage()
            } else {
                RestaurantMenu(
                    restaurant = restaurant,
                    onEditRestaurant = actions.onEditRestaurant,
                    onCreateMenuItem = actions.onCreateMenuItem,
                    onEditMenuItem = actions.onEditMenuItem,
                )
            }
        }
    }
}

@Composable
private fun RestaurantMenu(
    restaurant: Restaurant,
    onEditRestaurant: () -> Unit,
    onCreateMenuItem: () -> Unit,
    onEditMenuItem: (MenuItemId) -> Unit,
) {
    Text(
        text = restaurant.nome,
        modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = "Cardápio",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    RestaurantActions(
        onEditRestaurant = onEditRestaurant,
        onCreateMenuItem = onCreateMenuItem,
    )

    Spacer(Modifier.height(20.dp))

    if (restaurant.itens.isEmpty()) {
        Text(
            text = "Este restaurante ainda não possui itens no cardápio.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            restaurant.itens.forEach { item ->
                ItemLinha(
                    item = item,
                    onClick = { onEditMenuItem(item.id) },
                )
            }
        }
    }
}

@Composable
private fun RestaurantActions(
    onEditRestaurant: () -> Unit,
    onCreateMenuItem: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= ACTIONS_ROW_MIN_WIDTH) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                RestaurantActionButtons(onEditRestaurant, onCreateMenuItem)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ) {
                RestaurantActionButtons(onEditRestaurant, onCreateMenuItem)
            }
        }
    }
}

@Composable
private fun RestaurantActionButtons(
    onEditRestaurant: () -> Unit,
    onCreateMenuItem: () -> Unit,
) {
    TextButton(onClick = onEditRestaurant) {
        Text("Editar restaurante")
    }
    Button(onClick = onCreateMenuItem) {
        Text("Adicionar item")
    }
}

@Composable
private fun MissingRestaurantMessage() {
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Restaurante não encontrado",
        modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = "O endereço informado não corresponde a um restaurante disponível.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview
@Composable
private fun RestaurantDetailPreview() {
    DeliveryTheme(darkTheme = false) {
        RestaurantDetailScreen(
            restaurant = sampleRestaurants.first(),
            actions = RestaurantDetailActions({}, {}, {}, {}),
        )
    }
}

@Preview
@Composable
private fun EmptyRestaurantMenuPreview() {
    DeliveryTheme(darkTheme = false) {
        RestaurantDetailScreen(
            restaurant = sampleRestaurants.first().copy(itens = emptyList()),
            actions = RestaurantDetailActions({}, {}, {}, {}),
        )
    }
}

@Preview
@Composable
private fun MissingRestaurantPreview() {
    DeliveryTheme(darkTheme = true) {
        RestaurantDetailScreen(
            restaurant = null,
            actions = RestaurantDetailActions({}, {}, {}, {}),
        )
    }
}

private val ACTIONS_ROW_MIN_WIDTH = 400.dp
