package br.caio.delivery.app.presentation

import androidx.lifecycle.SavedStateHandle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FormViewModelsTest {
    @Test
    fun restaurantDraftRestoresValidationAndCanBeSavedAfterCorrection() {
        val savedState = SavedStateHandle()
        val form = RestaurantFormViewModel(savedState, "Restaurante anterior", true, true)
        form.updateName(" ")

        val restored = RestaurantFormViewModel(savedState, "Restaurante anterior", true, true)
        assertNotNull(restored.uiState.value.nameError)
        assertNull(restored.nameToSave())

        restored.updateName("  Sabor da Casa  ")
        assertEquals("Sabor da Casa", restored.nameToSave())
    }

    @Test
    fun menuItemRestoresEditedFieldsAndKeepsUnchangedValues() {
        val savedState = SavedStateHandle()
        val initialDraft = MenuItemDraft("Pizza", "Pizza da casa", "25,90", true)
        val form = MenuItemFormViewModel(savedState, initialDraft, true, true)
        form.updatePrice("inválido")
        form.updateAvailability(false)

        val restored = MenuItemFormViewModel(savedState, initialDraft, true, true)
        assertNotNull(restored.uiState.value.priceError)
        assertNull(restored.draftToSave())

        restored.updatePrice("30,00")
        assertEquals(initialDraft.copy(price = "30,00", available = false), restored.draftToSave())
    }
}
