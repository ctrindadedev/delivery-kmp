package br.caio.delivery.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.caio.delivery.app.theme.DeliveryTheme
import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId

@Composable
fun DeliveryApp(darkTheme: Boolean = isSystemInDarkTheme()) {
    var query by rememberSaveable { mutableStateOf("") }
    var restaurants by remember { mutableStateOf(sampleRestaurants) }

    DeliveryTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DeliveryNavigation(
                state = DeliveryUiState(restaurants = restaurants, query = query),
                actions =
                    DeliveryActions(
                        onQueryChange = { query = it },
                        onSaveRestaurant = { restaurantId, name ->
                            restaurants = restaurants.saveRestaurant(restaurantId, name)
                        },
                        onSaveMenuItem = { restaurantId, menuItemId, draft ->
                            draft.price.toCentsOrNull()?.let { priceInCents ->
                                restaurants =
                                    restaurants.saveMenuItem(
                                        restaurantId = restaurantId,
                                        menuItemId = menuItemId,
                                        draft = draft,
                                        priceInCents = priceInCents,
                                    )
                            }
                        },
                    ),
                modifier = Modifier.safeDrawingPadding(),
            )
        }
    }
}

private fun List<Restaurant>.saveRestaurant(
    restaurantId: RestaurantId?,
    name: String,
): List<Restaurant> =
    if (restaurantId == null) {
        this + Restaurant(id = RestaurantId.novo(), nome = name)
    } else {
        map { restaurant ->
            if (restaurant.id == restaurantId) restaurant.copy(nome = name) else restaurant
        }
    }

private fun List<Restaurant>.saveMenuItem(
    restaurantId: RestaurantId,
    menuItemId: MenuItemId?,
    draft: MenuItemDraft,
    priceInCents: Long,
): List<Restaurant> =
    map { restaurant ->
        if (restaurant.id != restaurantId) {
            restaurant
        } else {
            val item =
                MenuItem(
                    id = menuItemId ?: MenuItemId.novo(),
                    nome = draft.name.trim(),
                    descricao = draft.description.trim(),
                    preco = Dinheiro(priceInCents),
                    disponivel = draft.available,
                )
            val items =
                if (menuItemId == null) {
                    restaurant.itens + item
                } else {
                    restaurant.itens.map { current -> if (current.id == menuItemId) item else current }
                }
            restaurant.copy(itens = items)
        }
    }

private fun String.toCentsOrNull(): Long? {
    val parts = trim().replace(',', '.').split('.')
    val validParts = parts.size in 1..MAX_PRICE_PARTS && parts.all { it.isNotEmpty() && it.all(Char::isDigit) }
    return if (validParts) {
        val reais = parts[0].toLongOrNull()
        val cents =
            parts
                .getOrNull(1)
                ?.padEnd(2, '0')
                ?.takeIf { it.length == 2 }
                ?.toLongOrNull() ?: 0
        reais?.let { (it * CENTS_PER_REAL + cents).takeIf { total -> total > 0 } }
    } else {
        null
    }
}

private const val MAX_PRICE_PARTS = 2
private const val CENTS_PER_REAL = 100L
