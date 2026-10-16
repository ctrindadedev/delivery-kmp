package br.caio.delivery.app

import br.caio.delivery.dominio.ErroDeCampo
import br.caio.delivery.dominio.validarItem
import br.caio.delivery.dominio.validarRestaurante

data class RestaurantFormValidation(
    val nameError: String?,
) {
    val isValid: Boolean = nameError == null
}

data class MenuItemFormValidation(
    val nameError: String?,
    val descriptionError: String?,
    val priceError: String?,
) {
    val isValid: Boolean = nameError == null && descriptionError == null && priceError == null
}

fun validateRestaurantForm(name: String): RestaurantFormValidation {
    val errors = validarRestaurante(name)
    return RestaurantFormValidation(nameError = errors.messageFor("nome", "Nome"))
}

fun validateMenuItemForm(draft: MenuItemDraft): MenuItemFormValidation {
    val priceInCents = parsePriceInCents(draft.price)
    val errors = validarItem(draft.name, draft.description, priceInCents)
    val priceError =
        if (draft.price.isNotBlank() && priceInCents == null) {
            "Preço inválido. Use um valor como 25,90."
        } else {
            errors.messageFor("precoCentavos", "Preço")
        }
    return MenuItemFormValidation(
        nameError = errors.messageFor("nome", "Nome"),
        descriptionError = errors.messageFor("descricao", "Descrição"),
        priceError = priceError,
    )
}

fun parsePriceInCents(value: String): Long? {
    val match = PRICE_PATTERN.matchEntire(value.trim()) ?: return null
    val reais = match.groupValues[1].toLongOrNull()
    val cents =
        match.groupValues[2]
            .padEnd(2, '0')
            .ifEmpty { "0" }
            .toLongOrNull()
    return if (reais != null && cents != null && reais <= (Long.MAX_VALUE - cents) / CENTS_PER_REAL) {
        reais * CENTS_PER_REAL + cents
    } else {
        null
    }
}

private fun List<ErroDeCampo>.messageFor(
    field: String,
    label: String,
): String? = firstOrNull { it.campo == field }?.let { "$label ${it.mensagem}." }

private val PRICE_PATTERN = Regex("""^(\d+)(?:[,.](\d{1,2}))?$""")
private const val CENTS_PER_REAL = 100L
