package br.caio.delivery.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowWidthSizeClass
import br.caio.delivery.app.theme.DeliveryTheme
import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId

@Composable
fun DeliveryApp(darkTheme: Boolean = isSystemInDarkTheme()) {
    val widthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    val wide = widthSizeClass != WindowWidthSizeClass.COMPACT

    DeliveryTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Conteudo(
                largo = wide,
                modifier = Modifier.safeDrawingPadding(),
            )
        }
    }
}

@Composable
fun Conteudo(
    largo: Boolean,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var restaurants by remember { mutableStateOf(sampleRestaurants) }
    var selectedRestaurantRouteValue by rememberSaveable { mutableStateOf<String?>(null) }

    DeliveryNavigation(
        state =
            DeliveryUiState(
                restaurants = restaurants,
                query = query,
                selectedRestaurantId = selectedRestaurantRouteValue?.toRestaurantIdOrNull(),
            ),
        actions =
            DeliveryActions(
                onQueryChange = { query = it },
                onRestaurantSelected = { selectedRestaurantRouteValue = it.asRouteValue() },
                onSaveRestaurant = { restaurantId, name ->
                    restaurants = restaurants.saveRestaurant(restaurantId, name)
                },
                onSaveMenuItem = { restaurantId, menuItemId, draft ->
                    parsePriceInCents(draft.price)?.let { priceInCents ->
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
        largo = largo,
        modifier = modifier,
    )
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
