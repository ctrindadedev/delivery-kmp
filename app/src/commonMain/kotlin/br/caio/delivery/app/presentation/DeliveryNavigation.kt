package br.caio.delivery.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import br.caio.delivery.dominio.MenuItemId
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
    state: State<RestaurantListUiState>,
    actions: DeliveryActions,
    largo: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val wide = rememberUpdatedState(largo)
    val currentActions = rememberUpdatedState(actions)
    NavHost(
        navController = navController,
        startDestination = RestaurantListRoute,
        modifier = modifier,
    ) {
        restaurantListDestination(state, currentActions, wide, navController)
        restaurantDetailDestination(state, navController)
        restaurantFormDestinations(state, currentActions, navController)
        menuItemFormDestinations(state, currentActions, navController)
    }
}

private fun NavGraphBuilder.restaurantListDestination(
    state: State<RestaurantListUiState>,
    currentActions: State<DeliveryActions>,
    wide: State<Boolean>,
    navController: NavHostController,
) {
    composable<RestaurantListRoute> {
        val uiState = state.value
        val actions = currentActions.value
        if (wide.value) {
            RestaurantListDetailScreen(
                state = uiState,
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
                visibleRestaurants = uiState.visibleRestaurants,
                query = uiState.query,
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
    state: State<RestaurantListUiState>,
    navController: NavHostController,
) {
    composable<RestaurantDetailRoute>(
        deepLinks = listOf(navDeepLink<RestaurantDetailRoute>(basePath = RESTAURANT_DEEP_LINK_BASE)),
    ) { entry ->
        val route = entry.toRoute<RestaurantDetailRoute>()
        val restaurantId = route.restaurantId.toRestaurantIdOrNull()
        RestaurantDetailScreen(
            restaurant = state.value.restaurants.firstOrNull { it.id == restaurantId },
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
    state: State<RestaurantListUiState>,
    actions: State<DeliveryActions>,
    navController: NavHostController,
) {
    composable<RestaurantCreateRoute> {
        RestaurantFormRoute(
            onSubmit = { name -> actions.value.onSaveRestaurant(null, name) },
            onBack = { navController.popBackStack() },
        )
    }
    composable<RestaurantEditRoute> { entry ->
        val route = entry.toRoute<RestaurantEditRoute>()
        val restaurantId = route.restaurantId.toRestaurantIdOrNull()
        val restaurant = state.value.restaurants.firstOrNull { it.id == restaurantId }
        RestaurantFormRoute(
            initialName = restaurant?.nome.orEmpty(),
            onSubmit = { name -> restaurantId?.let { actions.value.onSaveRestaurant(it, name) } },
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
    val formViewModel =
        viewModel { RestaurantFormViewModel(createSavedStateHandle(), initialName, submitAvailable, editing) }
    val state by formViewModel.uiState.collectAsStateWithLifecycle()
    RestaurantFormScreen(
        state = state,
        actions =
            RestaurantFormActions(
                onNameChange = formViewModel::updateName,
                onSubmit = {
                    formViewModel.nameToSave()?.let {
                        onSubmit(it)
                        onBack()
                    }
                },
                onBack = onBack,
            ),
    )
}

private fun NavGraphBuilder.menuItemFormDestinations(
    state: State<RestaurantListUiState>,
    actions: State<DeliveryActions>,
    navController: NavHostController,
) {
    composable<MenuItemCreateRoute> { entry ->
        val restaurantId = entry.toRoute<MenuItemCreateRoute>().restaurantId.toRestaurantIdOrNull()
        MenuItemFormRoute(
            onSubmit = { draft -> restaurantId?.let { actions.value.onSaveMenuItem(it, null, draft) } },
            onBack = { navController.popBackStack() },
            submitAvailable = restaurantId != null,
        )
    }
    composable<MenuItemEditRoute> { entry ->
        val route = entry.toRoute<MenuItemEditRoute>()
        val restaurantId = route.restaurantId.toRestaurantIdOrNull()
        val menuItemId = route.menuItemId.toMenuItemIdOrNull()
        val restaurant = state.value.restaurants.firstOrNull { it.id == restaurantId }
        val item = restaurant?.itens?.firstOrNull { it.id == menuItemId }
        MenuItemFormRoute(
            initialDraft = item?.toDraft() ?: MenuItemDraft(),
            onSubmit = { draft ->
                if (restaurantId != null && menuItemId != null) {
                    actions.value.onSaveMenuItem(restaurantId, menuItemId, draft)
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
    val formViewModel =
        viewModel { MenuItemFormViewModel(createSavedStateHandle(), initialDraft, submitAvailable, editing) }
    val state by formViewModel.uiState.collectAsStateWithLifecycle()
    MenuItemFormScreen(
        state = state,
        actions =
            MenuItemFormActions(
                onNameChange = formViewModel::updateName,
                onDescriptionChange = formViewModel::updateDescription,
                onPriceChange = formViewModel::updatePrice,
                onAvailableChange = formViewModel::updateAvailability,
                onSubmit = {
                    formViewModel.draftToSave()?.let {
                        onSubmit(it)
                        onBack()
                    }
                },
                onBack = onBack,
            ),
    )
}
