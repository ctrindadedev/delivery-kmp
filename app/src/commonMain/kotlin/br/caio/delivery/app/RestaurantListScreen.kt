package br.caio.delivery.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RestaurantListScreen(
    restaurants: List<Restaurant>,
    query: String,
    onQueryChange: (String) -> Unit,
    onRestaurantClick: (RestaurantId) -> Unit,
) {
    val visibleRestaurants =
        remember(restaurants, query) {
            val normalizedQuery = query.trim()
            if (normalizedQuery.isEmpty()) {
                restaurants
            } else {
                restaurants.filter { it.nome.contains(normalizedQuery, ignoreCase = true) }
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 840.dp)) {
            Text(
                text = "Restaurantes perto de você",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Escolha um restaurante e descubra novos sabores.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar restaurante") },
                singleLine = true,
            )

            Spacer(Modifier.height(20.dp))

            if (visibleRestaurants.isEmpty()) {
                EmptyRestaurantList(query = query)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(visibleRestaurants, key = { it.id.toString() }) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick = { onRestaurantClick(restaurant.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val availableItems = restaurant.itens.filter { it.disponivel }
    val lowestPrice = availableItems.minByOrNull { it.preco.centavos }?.preco?.exibir()

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = restaurant.nome,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${availableItems.size} itens disponíveis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                lowestPrice?.let { price ->
                    Text(
                        text = "A partir de $price",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRestaurantList(
    query: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Nenhum restaurante encontrado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Não encontramos resultados para “$query”.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun RestaurantListPreview() {
    MaterialTheme {
        RestaurantListScreen(
            restaurants = sampleRestaurants,
            query = "",
            onQueryChange = {},
            onRestaurantClick = {},
        )
    }
}

@Preview
@Composable
private fun EmptyRestaurantListPreview() {
    MaterialTheme {
        RestaurantListScreen(
            restaurants = sampleRestaurants,
            query = "inexistente",
            onQueryChange = {},
            onRestaurantClick = {},
        )
    }
}
