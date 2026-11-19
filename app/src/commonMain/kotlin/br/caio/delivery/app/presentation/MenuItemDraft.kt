package br.caio.delivery.app.presentation

data class MenuItemDraft(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val available: Boolean = true,
)
