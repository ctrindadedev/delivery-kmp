package br.caio.delivery.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import br.caio.delivery.app.theme.DeliveryTheme

@Composable
fun DeliveryApp(darkTheme: Boolean = isSystemInDarkTheme()) {
    var query by rememberSaveable { mutableStateOf("") }

    DeliveryTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DeliveryNavigation(
                restaurants = sampleRestaurants,
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.safeDrawingPadding(),
            )
        }
    }
}
