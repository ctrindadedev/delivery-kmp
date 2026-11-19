package br.caio.delivery.app.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class RestaurantFormViewModel(
    private val savedState: SavedStateHandle,
    private val initialName: String,
    private val submitAvailable: Boolean,
    private val editing: Boolean,
) : ViewModel() {
    private val _uiState = MutableStateFlow(formState())
    val uiState = _uiState.asStateFlow()

    fun updateName(name: String) {
        savedState["name"] = name
        _uiState.value = formState()
    }

    fun nameToSave(): String? {
        val state = _uiState.value
        return if (state.submitEnabled) state.name.trim() else null
    }

    private fun formState(): RestaurantFormState {
        val enteredName = savedState.get<String>("name")
        val name = enteredName ?: initialName
        val validation = validateRestaurantForm(name)
        return RestaurantFormState(
            name = name,
            nameError = validation.nameError.takeIf { enteredName != null },
            submitEnabled = submitAvailable && validation.isValid,
            editing = editing,
        )
    }
}

internal class MenuItemFormViewModel(
    private val savedState: SavedStateHandle,
    private val initialDraft: MenuItemDraft,
    private val submitAvailable: Boolean,
    private val editing: Boolean,
) : ViewModel() {
    private val _uiState = MutableStateFlow(formState())
    val uiState = _uiState.asStateFlow()

    fun updateName(name: String) = update("name", name)

    fun updateDescription(description: String) = update("description", description)

    fun updatePrice(price: String) = update("price", price)

    fun updateAvailability(available: Boolean) = update("available", available)

    fun draftToSave(): MenuItemDraft? = _uiState.value.takeIf { it.submitEnabled }?.draft

    private fun <T> update(
        key: String,
        value: T,
    ) {
        savedState[key] = value
        _uiState.value = formState()
    }

    private fun formState(): MenuItemFormState {
        val name = savedState.get<String>("name")
        val description = savedState.get<String>("description")
        val price = savedState.get<String>("price")
        val draft =
            MenuItemDraft(
                name = name ?: initialDraft.name,
                description = description ?: initialDraft.description,
                price = price ?: initialDraft.price,
                available = savedState.get<Boolean>("available") ?: initialDraft.available,
            )
        val validation = validateMenuItemForm(draft)
        return MenuItemFormState(
            draft = draft,
            nameError = validation.nameError.takeIf { name != null },
            descriptionError = validation.descriptionError.takeIf { description != null },
            priceError = validation.priceError.takeIf { price != null },
            submitEnabled = submitAvailable && validation.isValid,
            editing = editing,
        )
    }
}
