package br.caio.delivery.app.presentation

import br.caio.delivery.app.data.InMemoryRestaurantRepository
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlin.test.Test
import kotlin.test.assertEquals

class RestaurantListViewModelTest {
    @Test
    fun initialStateContainsRestaurantsAndEmptyQuery() {
        val restaurant = Restaurant(id = RestaurantId.novo(), nome = "Sabor da Casa")
        val viewModel = RestaurantListViewModel(InMemoryRestaurantRepository(listOf(restaurant)))

        assertEquals("", viewModel.uiState.value.query)
        assertEquals(listOf(restaurant), viewModel.uiState.value.visibleRestaurants)
    }

    @Test
    fun searchIsCaseInsensitiveAndIgnoresSurroundingSpaces() {
        val sushi = Restaurant(id = RestaurantId.novo(), nome = "Sushi Natal")
        val pizza = Restaurant(id = RestaurantId.novo(), nome = "Pizzaria")
        val viewModel = RestaurantListViewModel(InMemoryRestaurantRepository(listOf(sushi, pizza)))

        viewModel.updateQuery("   sUsHi  ")

        assertEquals("   sUsHi  ", viewModel.uiState.value.query)
        assertEquals(listOf(sushi), viewModel.uiState.value.visibleRestaurants)
    }

    @Test
    fun updatingRestaurantsKeepsQueryAndSelection() {
        val original = Restaurant(id = RestaurantId.novo(), nome = "Sushi Original")
        val viewModel = RestaurantListViewModel(InMemoryRestaurantRepository(listOf(original)))

        viewModel.updateQuery("sushi")
        viewModel.selectRestaurant(original.id)
        viewModel.saveRestaurant(null, "Sushi Novo")

        assertEquals(original.id, viewModel.uiState.value.selectedRestaurantId)
        val visible = viewModel.uiState.value.visibleRestaurants
        val names = visible.map { it.nome }
        assertEquals(listOf(original.nome, "Sushi Novo"), names)
    }

    @Test
    fun emptyQueryRestoresAllRestaurants() {
        val restaurant = Restaurant(id = RestaurantId.novo(), nome = "Bistrô")
        val viewModel = RestaurantListViewModel(InMemoryRestaurantRepository(listOf(restaurant)))

        viewModel.updateQuery("não existe")
        assertEquals(emptyList(), viewModel.uiState.value.visibleRestaurants)
        viewModel.updateQuery("  ")

        assertEquals(listOf(restaurant), viewModel.uiState.value.visibleRestaurants)
    }
}
