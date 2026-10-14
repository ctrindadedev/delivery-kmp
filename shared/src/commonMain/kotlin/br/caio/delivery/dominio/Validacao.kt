package br.caio.delivery.dominio

data class ErroDeCampo(
    val campo: String,
    val mensagem: String,
)

const val NOME_TAMANHO_MAXIMO = 120
const val DESCRICAO_TAMANHO_MAXIMO = 500

fun validarRestaurante(nome: String): List<ErroDeCampo> = listOfNotNull(validarNome(nome))

fun validarItem(
    nome: String,
    descricao: String,
    precoCentavos: Long?,
): List<ErroDeCampo> =
    listOfNotNull(
        validarNome(nome),
        ErroDeCampo("descricao", "deve ter no máximo $DESCRICAO_TAMANHO_MAXIMO caracteres")
            .takeIf { descricao.trim().length > DESCRICAO_TAMANHO_MAXIMO },
        when {
            precoCentavos == null -> ErroDeCampo("precoCentavos", "é obrigatório")
            precoCentavos <= 0 -> ErroDeCampo("precoCentavos", "deve ser maior que zero")
            else -> null
        },
    )

private fun validarNome(nome: String): ErroDeCampo? =
    when {
        nome.isBlank() -> ErroDeCampo("nome", "é obrigatório")
        nome.trim().length > NOME_TAMANHO_MAXIMO -> ErroDeCampo("nome", "deve ter no máximo $NOME_TAMANHO_MAXIMO caracteres")
        else -> null
    }
