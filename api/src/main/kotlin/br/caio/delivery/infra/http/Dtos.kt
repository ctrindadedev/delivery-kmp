package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.DadosDoItem
import br.caio.delivery.aplicacao.PaginaDeResultados
import br.caio.delivery.aplicacao.exigirValidos
import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.validarItem
import br.caio.delivery.dominio.validarRestaurante
import kotlinx.serialization.Serializable

@Serializable
data class RestauranteRequisicao(
    val nome: String,
) {
    fun nomeValido(): String = nome.also { exigirValidos(validarRestaurante(it)) }
}

@Serializable
data class RestauranteResposta(
    val id: String,
    val nome: String,
)

@Serializable
data class NovoItemRequisicao(
    val nome: String,
    val descricao: String = "",
    val precoCentavos: Long,
    val disponivel: Boolean = true,
) {
    fun paraDados() = dadosDoItem(nome, descricao, precoCentavos, disponivel)
}

@Serializable
data class ItemRequisicao(
    val nome: String,
    val descricao: String = "",
    val precoCentavos: Long,
    val disponivel: Boolean,
) {
    fun paraDados() = dadosDoItem(nome, descricao, precoCentavos, disponivel)
}

private fun dadosDoItem(
    nome: String,
    descricao: String,
    precoCentavos: Long,
    disponivel: Boolean,
): DadosDoItem {
    exigirValidos(validarItem(nome, descricao, precoCentavos))
    return DadosDoItem(nome, descricao, Dinheiro(precoCentavos), disponivel)
}

@Serializable
data class ItemResposta(
    val id: String,
    val nome: String,
    val descricao: String,
    val precoCentavos: Long,
    val precoFormatado: String,
    val disponivel: Boolean,
)

@Serializable
data class PaginaResposta<T>(
    val itens: List<T>,
    val pagina: Int,
    val tamanho: Int,
    val total: Long,
)

fun Restaurant.paraResposta() = RestauranteResposta(id = id.value.toString(), nome = nome)

fun MenuItem.paraResposta() =
    ItemResposta(
        id = id.value.toString(),
        nome = nome,
        descricao = descricao,
        precoCentavos = preco.centavos,
        precoFormatado = preco.exibir(),
        disponivel = disponivel,
    )

fun <T, R> PaginaDeResultados<T>.paraResposta(conversao: (T) -> R) =
    PaginaResposta(
        itens = itens.map(conversao),
        pagina = pagina.numero,
        tamanho = pagina.tamanho,
        total = total,
    )
