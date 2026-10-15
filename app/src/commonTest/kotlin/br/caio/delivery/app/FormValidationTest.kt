package br.caio.delivery.app

import br.caio.delivery.dominio.DESCRICAO_TAMANHO_MAXIMO
import br.caio.delivery.dominio.NOME_TAMANHO_MAXIMO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormValidationTest {
    @Test
    fun `restaurante exige nome dentro do limite do dominio`() {
        assertFalse(validateRestaurantForm("   ").isValid)
        assertNotNull(validateRestaurantForm("a".repeat(NOME_TAMANHO_MAXIMO + 1)).nameError)
        assertTrue(validateRestaurantForm("Cantina da Nonna").isValid)
    }

    @Test
    fun `item exige nome e preco positivo mas aceita descricao vazia`() {
        val valid = validateMenuItemForm(MenuItemDraft(name = "Café", price = "5,50"))
        val invalid = validateMenuItemForm(MenuItemDraft(name = "", price = "0"))

        assertTrue(valid.isValid)
        assertFalse(invalid.isValid)
        assertNotNull(invalid.nameError)
        assertNotNull(invalid.priceError)
    }

    @Test
    fun `descricao respeita o mesmo limite da api`() {
        val validation =
            validateMenuItemForm(
                MenuItemDraft(
                    name = "Café",
                    description = "a".repeat(DESCRICAO_TAMANHO_MAXIMO + 1),
                    price = "5,00",
                ),
            )

        assertNotNull(validation.descriptionError)
    }

    @Test
    fun `preco aceita virgula ou ponto e rejeita formato invalido`() {
        assertEquals(2_590, parsePriceInCents("25,90"))
        assertEquals(2_590, parsePriceInCents("25.90"))
        assertEquals(2_500, parsePriceInCents("25"))
        assertNull(parsePriceInCents("25,999"))
        assertNull(parsePriceInCents("vinte"))
    }
}
