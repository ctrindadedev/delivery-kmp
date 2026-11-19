package br.caio.delivery.app.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.caio.delivery.app.data.InMemoryRestaurantRepository
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RestaurantListFlowTest {
    @Test
    fun restaurantSavedInViewModelAppearsInFilteredList() =
        runComposeUiTest {
            val pizzaria = Restaurant(id = RestaurantId.novo(), nome = "Pizzaria")
            val viewModel = RestaurantListViewModel(InMemoryRestaurantRepository(listOf(pizzaria)))
            setContent {
                val owner = remember { TestLifecycleOwner() }
                CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                    MaterialTheme {
                        Conteudo(listViewModel = viewModel, largo = false)
                    }
                }
            }

            onNodeWithText("Buscar restaurante").performTextInput("bistrô")
            onNodeWithText("Nenhum restaurante encontrado").assertIsDisplayed()
            runOnIdle { viewModel.saveRestaurant(null, "Bistrô do Porto") }
            onNodeWithText("Bistrô do Porto").assertIsDisplayed()
        }
}

private class TestLifecycleOwner : LifecycleOwner {
    override val lifecycle =
        LifecycleRegistry.createUnsafe(this).apply {
            currentState = Lifecycle.State.RESUMED
        }
}
