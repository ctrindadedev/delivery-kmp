package br.caio.delivery.aplicacao

data class Pagina(
    val numero: Int = 1,
    val tamanho: Int = TAMANHO_PADRAO,
) {
    init {
        require(numero >= 1) { "pagina deve ser maior ou igual a 1" }
        require(tamanho in 1..TAMANHO_MAXIMO) { "tamanho deve estar entre 1 e $TAMANHO_MAXIMO" }
    }

    val deslocamento: Long get() = (numero - 1).toLong() * tamanho

    companion object {
        const val TAMANHO_PADRAO = 20
        const val TAMANHO_MAXIMO = 100
    }
}

data class PaginaDeResultados<T>(
    val itens: List<T>,
    val pagina: Pagina,
    val total: Long,
)
