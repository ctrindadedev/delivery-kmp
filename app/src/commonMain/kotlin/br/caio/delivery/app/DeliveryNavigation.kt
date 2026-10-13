package br.caio.delivery.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
object RestaurantListRoute

@Serializable
data class RestaurantDetailRoute(
    val restaurantId: String,
)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun DeliveryNavigation(
    restaurants: List<Restaurant>,
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = RestaurantListRoute,
        modifier = modifier,
    ) {
        composable<RestaurantListRoute> {
            RestaurantListScreen(
                restaurants = restaurants,
                query = query,
                onQueryChange = onQueryChange,
                onRestaurantClick = { restaurantId ->
                    navController.navigate(RestaurantDetailRoute(restaurantId.value.toString()))
                },
            )
        }

        composable<RestaurantDetailRoute>(
            deepLinks =
                listOf(
                    navDeepLink<RestaurantDetailRoute>(basePath = RESTAURANT_DEEP_LINK_BASE),
                ),
        ) { entry ->
            val route = entry.toRoute<RestaurantDetailRoute>()
            val restaurantId = route.restaurantId.toRestaurantIdOrNull()
            RestaurantDetailScreen(
                restaurant = restaurants.firstOrNull { it.id == restaurantId },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun String.toRestaurantIdOrNull(): RestaurantId? = runCatching { RestaurantId(Uuid.parse(this)) }.getOrNull()

const val RESTAURANT_DEEP_LINK_BASE = "delivery://restaurantes"
