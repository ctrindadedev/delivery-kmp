package br.caio.delivery.app

import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import kotlin.test.Test
import kotlin.test.assertEquals

class RestaurantListViewModelTest {
    @Test
    fun initialStateContainsRestaurantsAndEmptyQuery() {
        val restaurant = Restaurant(id = RestaurantId.novo(), nome = "Sabor da Casa")
        val viewModel = RestaurantListViewModel(listOf(restaurant))

        assertEquals("", viewModel.uiState.value.query)
        assertEquals(listOf(restaurant), viewModel.uiState.value.visibleRestaurants)
    }

    @Test
    fun searchIsCaseInsensitiveAndIgnoresSurroundingSpaces() {
        val sushi = Restaurant(id = RestaurantId.novo(), nome = "Sushi Natal")
        val pizza = Restaurant(id = RestaurantId.novo(), nome = "Pizzaria")
        val viewModel = RestaurantListViewModel(listOf(sushi, pizza))

        viewModel.updateQuery("   sUsHi  ")

        assertEquals("   sUsHi  ", viewModel.uiState.value.query)
        assertEquals(listOf(sushi), viewModel.uiState.value.visibleRestaurants)
    }

    @Test
    fun updatingRestaurantsKeepsQueryAndSelection() {
        val original = Restaurant(id = RestaurantId.novo(), nome = "Sushi Original")
        val newRestaurant = Restaurant(id = RestaurantId.novo(), nome = "Sushi Novo")
        val viewModel = RestaurantListViewModel(listOf(original))

        viewModel.updateQuery("sushi")
        viewModel.selectRestaurant(original.id)
        viewModel.updateRestaurants(listOf(original, newRestaurant))

        assertEquals(original.id, viewModel.uiState.value.selectedRestaurantId)
        assertEquals(listOf(original, newRestaurant), viewModel.uiState.value.visibleRestaurants)
    }

    @Test
    fun emptyQueryRestoresAllRestaurants() {
        val restaurant = Restaurant(id = RestaurantId.novo(), nome = "Bistrô")
        val viewModel = RestaurantListViewModel(listOf(restaurant))

        viewModel.updateQuery("não existe")
        assertEquals(emptyList(), viewModel.uiState.value.visibleRestaurants)
        viewModel.updateQuery("  ")

        assertEquals(listOf(restaurant), viewModel.uiState.value.visibleRestaurants)
    }
}
