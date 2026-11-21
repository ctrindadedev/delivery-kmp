package br.caio.delivery.app.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import br.caio.delivery.app.theme.DeliveryTheme

@Composable
fun DeliveryScreen(
    listViewModel: RestaurantListViewModel,
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    val widthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    val wide = widthSizeClass != WindowWidthSizeClass.COMPACT

    DeliveryTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Conteudo(
                listViewModel = listViewModel,
                largo = wide,
                modifier = Modifier.safeDrawingPadding(),
            )
        }
    }
}

@Composable
fun Conteudo(
    listViewModel: RestaurantListViewModel,
    largo: Boolean,
    modifier: Modifier = Modifier,
) {
    DeliveryNavigation(
        state = listViewModel.uiState.collectAsStateWithLifecycle(),
        actions =
            DeliveryActions(
                onQueryChange = listViewModel::updateQuery,
                onRetry = listViewModel::reload,
                onRestaurantSelected = listViewModel::selectRestaurant,
                onSaveRestaurant = listViewModel::saveRestaurant,
                onSaveMenuItem = listViewModel::saveMenuItem,
            ),
        largo = largo,
        modifier = modifier,
    )
}
