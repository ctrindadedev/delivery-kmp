package br.caio.delivery.app.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.caio.delivery.app.sampleRestaurants
import br.caio.delivery.app.theme.DeliveryTheme
import br.caio.delivery.dominio.Restaurant
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RestaurantListScreen(
    state: RestaurantListUiState,
    actions: RestaurantListActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 840.dp)) {
            RestaurantListContent(
                state = state,
                actions = actions,
            )
        }
    }
}

@Composable
private fun ColumnScope.RestaurantListContent(
    state: RestaurantListUiState,
    actions: RestaurantListActions,
) {
    Text(
        text = "Restaurantes perto de você",
        modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = "Escolha um restaurante e descubra novos sabores.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(Modifier.height(20.dp))

    when (state.status) {
        RestaurantListStatus.Loading -> {
            CircularProgressIndicator(
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .semantics { contentDescription = "Carregando restaurantes" },
            )
        }
        RestaurantListStatus.Error -> {
            Text("Não foi possível carregar os restaurantes.")
            Button(onClick = actions.onRetry) { Text("Tentar novamente") }
        }
        RestaurantListStatus.Empty, RestaurantListStatus.Success -> RestaurantCatalog(state, actions)
    }
}

@Composable
private fun ColumnScope.RestaurantCatalog(
    state: RestaurantListUiState,
    actions: RestaurantListActions,
) {
    CampoTexto(
        value = state.query,
        onValueChange = actions.onQueryChange,
        label = "Buscar restaurante",
    )

    Spacer(Modifier.height(12.dp))

    Button(
        onClick = actions.onCreateRestaurant,
        modifier = Modifier.align(Alignment.End),
    ) {
        Text("Cadastrar restaurante")
    }

    Spacer(Modifier.height(20.dp))

    val visibleRestaurants = state.visibleRestaurants
    if (visibleRestaurants.isEmpty()) {
        EmptyRestaurantList(query = state.query)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(visibleRestaurants, key = { it.id.toString() }) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onClick = { actions.onRestaurantClick(restaurant.id) },
                )
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
                .sizeIn(minHeight = 48.dp)
                .clickable(
                    onClickLabel = "Abrir cardápio de ${restaurant.nome}",
                    role = Role.Button,
                    onClick = onClick,
                ),
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
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text =
                    if (query.isBlank()) {
                        "Cadastre um restaurante para começar."
                    } else {
                        "Não encontramos resultados para “$query”."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun RestaurantListPreview() {
    DeliveryTheme(darkTheme = false) {
        RestaurantListScreen(
            state = RestaurantListUiState(restaurants = sampleRestaurants, status = RestaurantListStatus.Success),
            actions = RestaurantListActions({}, {}, {}, {}),
        )
    }
}

@Preview
@Composable
private fun EmptyRestaurantListPreview() {
    DeliveryTheme(darkTheme = true) {
        RestaurantListScreen(
            state = RestaurantListUiState(status = RestaurantListStatus.Empty),
            actions = RestaurantListActions({}, {}, {}, {}),
        )
    }
}
