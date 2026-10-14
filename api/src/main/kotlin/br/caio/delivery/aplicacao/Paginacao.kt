package br.caio.delivery.aplicacao

import br.caio.delivery.dominio.ErroDeCampo

data class Pagina(
    val numero: Int = 1,
    val tamanho: Int = TAMANHO_PADRAO,
) {
    init {
        exigirValidos(validar(numero, tamanho))
    }

    val deslocamento: Long get() = (numero - 1).toLong() * tamanho

    companion object {
        const val TAMANHO_PADRAO = 20
        const val TAMANHO_MAXIMO = 100

        fun validar(
            numero: Int,
            tamanho: Int,
        ): List<ErroDeCampo> =
            listOfNotNull(
                ErroDeCampo("pagina", "deve ser maior ou igual a 1").takeIf { numero < 1 },
                ErroDeCampo("tamanho", "deve estar entre 1 e $TAMANHO_MAXIMO").takeIf { tamanho !in 1..TAMANHO_MAXIMO },
            )
    }
}

data class PaginaDeResultados<T>(
    val itens: List<T>,
    val pagina: Pagina,
    val total: Long,
)
