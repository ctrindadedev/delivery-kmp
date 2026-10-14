package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.DadosDoItem
import br.caio.delivery.aplicacao.PaginaDeResultados
import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.Restaurant
import kotlinx.serialization.Serializable

@Serializable
data class RestauranteRequisicao(
    val nome: String,
)

@Serializable
data class RestauranteResposta(
    val id: String,
    val nome: String,
)

@Serializable
data class ItemRequisicao(
    val nome: String,
    val descricao: String = "",
    val precoCentavos: Long,
    val disponivel: Boolean = true,
) {
    fun paraDados() = DadosDoItem(nome, descricao, Dinheiro(precoCentavos), disponivel)
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

@Serializable
data class ErroResposta(
    val erro: String,
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
