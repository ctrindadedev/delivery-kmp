package br.caio.delivery.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlinx.serialization.Serializable

@Serializable
object RestaurantListRoute

@Serializable
data class RestaurantDetailRoute(
    val restaurantId: String,
)

@Serializable
object RestaurantCreateRoute

@Serializable
data class RestaurantEditRoute(
    val restaurantId: String,
)

@Serializable
data class MenuItemCreateRoute(
    val restaurantId: String,
)

@Serializable
data class MenuItemEditRoute(
    val restaurantId: String,
    val menuItemId: String,
)

data class DeliveryUiState(
    val restaurants: List<Restaurant>,
    val query: String,
    val selectedRestaurantId: RestaurantId?,
)

data class DeliveryActions(
    val onQueryChange: (String) -> Unit,
    val onRestaurantSelected: (RestaurantId) -> Unit,
    val onSaveRestaurant: (RestaurantId?, String) -> Unit,
    val onSaveMenuItem: (RestaurantId, MenuItemId?, MenuItemDraft) -> Unit,
)

data class WideNavigationActions(
    val onCreateRestaurant: () -> Unit,
    val onEditRestaurant: (RestaurantId) -> Unit,
    val onCreateMenuItem: (RestaurantId) -> Unit,
    val onEditMenuItem: (RestaurantId, MenuItemId) -> Unit,
)

@Composable
fun DeliveryNavigation(
    state: DeliveryUiState,
    actions: DeliveryActions,
    largo: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = RestaurantListRoute,
        modifier = modifier,
    ) {
        restaurantListDestination(state, actions, largo, navController)
        restaurantDetailDestination(state.restaurants, navController)
        restaurantFormDestinations(state.restaurants, actions, navController)
        menuItemFormDestinations(state.restaurants, actions, navController)
    }
}

private fun NavGraphBuilder.restaurantListDestination(
    state: DeliveryUiState,
    actions: DeliveryActions,
    largo: Boolean,
    navController: NavHostController,
) {
    composable<RestaurantListRoute> {
        if (largo) {
            RestaurantListDetailScreen(
                state = state,
                actions = actions,
                navigationActions =
                    WideNavigationActions(
                        onCreateRestaurant = { navController.navigate(RestaurantCreateRoute) },
                        onEditRestaurant = { restaurantId ->
                            navController.navigate(RestaurantEditRoute(restaurantId.asRouteValue()))
                        },
                        onCreateMenuItem = { restaurantId ->
                            navController.navigate(MenuItemCreateRoute(restaurantId.asRouteValue()))
                        },
                        onEditMenuItem = { restaurantId, menuItemId ->
                            navController.navigate(
                                MenuItemEditRoute(restaurantId.asRouteValue(), menuItemId.asRouteValue()),
                            )
                        },
                    ),
            )
        } else {
            RestaurantListScreen(
                restaurants = state.restaurants,
                query = state.query,
                actions =
                    RestaurantListActions(
                        onQueryChange = actions.onQueryChange,
                        onRestaurantClick = { restaurantId ->
                            navController.navigate(RestaurantDetailRoute(restaurantId.asRouteValue()))
                        },
                        onCreateRestaurant = { navController.navigate(RestaurantCreateRoute) },
                    ),
            )
        }
    }
}

private fun NavGraphBuilder.restaurantDetailDestination(
    restaurants: List<Restaurant>,
    navController: NavHostController,
) {
    composable<RestaurantDetailRoute>(
        deepLinks = listOf(navDeepLink<RestaurantDetailRoute>(basePath = RESTAURANT_DEEP_LINK_BASE)),
    ) { entry ->
        val route = entry.toRoute<RestaurantDetailRoute>()
        val restaurantId = route.restaurantId.toRestaurantIdOrNull()
        RestaurantDetailScreen(
            restaurant = restaurants.firstOrNull { it.id == restaurantId },
            actions =
                RestaurantDetailActions(
                    onBack = { navController.popBackStack() },
                    onEditRestaurant = {
                        restaurantId?.let { navController.navigate(RestaurantEditRoute(it.asRouteValue())) }
                    },
                    onCreateMenuItem = {
                        restaurantId?.let { navController.navigate(MenuItemCreateRoute(it.asRouteValue())) }
                    },
                    onEditMenuItem = { menuItemId ->
                        restaurantId?.let {
                            navController.navigate(
                                MenuItemEditRoute(it.asRouteValue(), menuItemId.asRouteValue()),
                            )
                        }
                    },
                ),
        )
    }
}

private fun NavGraphBuilder.restaurantFormDestinations(
    restaurants: List<Restaurant>,
    actions: DeliveryActions,
    navController: NavHostController,
) {
    composable<RestaurantCreateRoute> {
        RestaurantFormRoute(
            onSubmit = { name -> actions.onSaveRestaurant(null, name) },
            onBack = { navController.popBackStack() },
        )
    }
    composable<RestaurantEditRoute> { entry ->
        val route = entry.toRoute<RestaurantEditRoute>()
        val restaurantId = route.restaurantId.toRestaurantIdOrNull()
        RestaurantFormRoute(
            initialName = restaurants.firstOrNull { it.id == restaurantId }?.nome.orEmpty(),
            onSubmit = { name -> restaurantId?.let { actions.onSaveRestaurant(it, name) } },
            onBack = { navController.popBackStack() },
            submitAvailable = restaurantId != null,
            editing = true,
        )
    }
}

@Composable
private fun RestaurantFormRoute(
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
    initialName: String = "",
    submitAvailable: Boolean = true,
    editing: Boolean = false,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var nameTouched by rememberSaveable { mutableStateOf(false) }
    val validation = validateRestaurantForm(name)
    RestaurantFormScreen(
        state =
            RestaurantFormState(
                name = name,
                nameError = validation.nameError.takeIf { nameTouched },
                submitEnabled = submitAvailable && validation.isValid,
                editing = editing,
            ),
        actions =
            RestaurantFormActions(
                onNameChange = {
                    name = it
                    nameTouched = true
                },
                onSubmit = {
                    onSubmit(name.trim())
                    onBack()
                },
                onBack = onBack,
            ),
    )
}

private fun NavGraphBuilder.menuItemFormDestinations(
    restaurants: List<Restaurant>,
    actions: DeliveryActions,
    navController: NavHostController,
) {
    composable<MenuItemCreateRoute> { entry ->
        val restaurantId = entry.toRoute<MenuItemCreateRoute>().restaurantId.toRestaurantIdOrNull()
        MenuItemFormRoute(
            onSubmit = { draft -> restaurantId?.let { actions.onSaveMenuItem(it, null, draft) } },
            onBack = { navController.popBackStack() },
            submitAvailable = restaurantId != null,
        )
    }
    composable<MenuItemEditRoute> { entry ->
        val route = entry.toRoute<MenuItemEditRoute>()
        val restaurantId = route.restaurantId.toRestaurantIdOrNull()
        val menuItemId = route.menuItemId.toMenuItemIdOrNull()
        val item = restaurants.firstOrNull { it.id == restaurantId }?.itens?.firstOrNull { it.id == menuItemId }
        MenuItemFormRoute(
            initialDraft = item?.toDraft() ?: MenuItemDraft(),
            onSubmit = { draft ->
                if (restaurantId != null && menuItemId != null) {
                    actions.onSaveMenuItem(restaurantId, menuItemId, draft)
                }
            },
            onBack = { navController.popBackStack() },
            submitAvailable = restaurantId != null && menuItemId != null,
            editing = true,
        )
    }
}

@Composable
private fun MenuItemFormRoute(
    onSubmit: (MenuItemDraft) -> Unit,
    onBack: () -> Unit,
    submitAvailable: Boolean,
    initialDraft: MenuItemDraft = MenuItemDraft(),
    editing: Boolean = false,
) {
    var name by rememberSaveable { mutableStateOf(initialDraft.name) }
    var description by rememberSaveable { mutableStateOf(initialDraft.description) }
    var price by rememberSaveable { mutableStateOf(initialDraft.price) }
    var available by rememberSaveable { mutableStateOf(initialDraft.available) }
    var nameTouched by rememberSaveable { mutableStateOf(false) }
    var descriptionTouched by rememberSaveable { mutableStateOf(false) }
    var priceTouched by rememberSaveable { mutableStateOf(false) }
    val draft = MenuItemDraft(name, description, price, available)
    val validation = validateMenuItemForm(draft)
    MenuItemFormScreen(
        state =
            MenuItemFormState(
                draft = draft,
                nameError = validation.nameError.takeIf { nameTouched },
                descriptionError = validation.descriptionError.takeIf { descriptionTouched },
                priceError = validation.priceError.takeIf { priceTouched },
                submitEnabled = submitAvailable && validation.isValid,
                editing = editing,
            ),
        actions =
            MenuItemFormActions(
                onNameChange = {
                    name = it
                    nameTouched = true
                },
                onDescriptionChange = {
                    description = it
                    descriptionTouched = true
                },
                onPriceChange = {
                    price = it
                    priceTouched = true
                },
                onAvailableChange = { available = it },
                onSubmit = {
                    onSubmit(draft)
                    onBack()
                },
                onBack = onBack,
            ),
    )
}
