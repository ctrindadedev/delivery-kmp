package br.caio.delivery.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.caio.delivery.app.theme.DeliveryTheme
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.Restaurant
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant?,
    onBack: () -> Unit,
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
            TextButton(onClick = onBack) {
                Text("Voltar")
            }

            if (restaurant == null) {
                MissingRestaurantMessage()
            } else {
                RestaurantMenu(restaurant)
            }
        }
    }
}

@Composable
private fun RestaurantMenu(restaurant: Restaurant) {
    Text(
        text = restaurant.nome,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = "Cardápio",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                MenuItemCard(item)
            }
        }
    }
}

@Composable
private fun MenuItemCard(item: MenuItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = item.preco.exibir(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!item.disponivel) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Indisponível",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun MissingRestaurantMessage() {
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Restaurante não encontrado",
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
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun MissingRestaurantPreview() {
    DeliveryTheme(darkTheme = true) {
        RestaurantDetailScreen(
            restaurant = null,
            onBack = {},
        )
    }
}
