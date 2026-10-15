package br.caio.delivery.app

data class MenuItemDraft(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val available: Boolean = true,
)
