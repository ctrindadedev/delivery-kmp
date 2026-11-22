package br.caio.delivery.app

import br.caio.delivery.app.data.InMemoryRestaurantRepository
import br.caio.delivery.app.presentation.MenuItemDraft
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryCompositionTest {
    @Test
    fun compositionCreatesViewModelWithIsolatedCatalogAndWorkingMenuWrites() =
        runViewModelTest {
            val viewModel = createListViewModel(InMemoryRestaurantRepository(sampleRestaurants))
            runCurrent()
            val initial = viewModel.uiState.value
            val restaurant = initial.restaurants.first()
            val draft = MenuItemDraft("Suco de caju", "Sem açúcar", "8,50", true)

            viewModel.saveMenuItem(restaurant.id, null, draft)

            val current = viewModel.uiState.value
            val updated = current.restaurants.first()
            val item = assertNotNull(updated.itens.find { it.nome == draft.name })
            assertEquals(850L, item.preco.centavos)

            val fresh = createListViewModel(InMemoryRestaurantRepository(sampleRestaurants))
            runCurrent()
            val freshState = fresh.uiState.value
            val freshRestaurant = freshState.restaurants.first()
            assertTrue(freshRestaurant.itens.none { it.id == item.id })
        }
}
