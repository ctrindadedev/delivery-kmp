package br.caio.delivery.dominio

import kotlin.test.Test
import kotlin.test.assertEquals

class ValidacaoTest {
    @Test
    fun restauranteComNomeValidoNaoTemErros() {
        assertEquals(emptyList(), validarRestaurante("Cantina"))
    }

    @Test
    fun nomeEmBrancoEObrigatorio() {
        assertEquals(listOf(ErroDeCampo("nome", "é obrigatório")), validarRestaurante("   "))
        assertEquals(listOf("nome"), validarItem("", "", 100).map { it.campo })
    }

    @Test
    fun nomeConsideraOTamanhoSemEspacosNasPontas() {
        assertEquals(emptyList(), validarRestaurante("  " + "a".repeat(NOME_TAMANHO_MAXIMO) + "  "))
        assertEquals(listOf("nome"), validarRestaurante("a".repeat(NOME_TAMANHO_MAXIMO + 1)).map { it.campo })
    }

    @Test
    fun itemValidoNaoTemErros() {
        assertEquals(emptyList(), validarItem("Lasanha", "Massa fresca", 4500))
    }

    @Test
    fun precoDoItemEObrigatorioEPositivo() {
        assertEquals(listOf(ErroDeCampo("precoCentavos", "é obrigatório")), validarItem("Lasanha", "", null))
        assertEquals(listOf(ErroDeCampo("precoCentavos", "deve ser maior que zero")), validarItem("Lasanha", "", 0))
        assertEquals(listOf(ErroDeCampo("precoCentavos", "deve ser maior que zero")), validarItem("Lasanha", "", -1))
    }

    @Test
    fun descricaoTemTamanhoMaximo() {
        val erros = validarItem("Lasanha", "a".repeat(DESCRICAO_TAMANHO_MAXIMO + 1), 4500)

        assertEquals(listOf("descricao"), erros.map { it.campo })
    }

    @Test
    fun reuneTodosOsErrosDoItem() {
        val erros = validarItem(" ", "a".repeat(DESCRICAO_TAMANHO_MAXIMO + 1), 0)

        assertEquals(listOf("nome", "descricao", "precoCentavos"), erros.map { it.campo })
    }
}
