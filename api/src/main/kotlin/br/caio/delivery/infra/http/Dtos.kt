package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.DadosDoItem
import br.caio.delivery.aplicacao.PaginaDeResultados
import br.caio.delivery.aplicacao.exigirValidos
import br.caio.delivery.dominio.Dinheiro
import br.caio.delivery.dominio.ErroDeCampo
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.validarItem
import br.caio.delivery.dominio.validarRestaurante
import kotlinx.serialization.Serializable

@Serializable
data class RestauranteRequisicao(
    val nome: String? = null,
) {
    fun nomeValido(): String = nome.orEmpty().also { exigirValidos(validarRestaurante(it)) }
}

@Serializable
data class RestauranteResposta(
    val id: String,
    val nome: String,
)

@Serializable
data class ItemRequisicao(
    val nome: String? = null,
    val descricao: String? = null,
    val precoCentavos: Long? = null,
    val disponivel: Boolean? = null,
) {
    fun paraCriacao() = paraDados(disponivelSeAusente = true)

    fun paraAtualizacao() = paraDados(disponivelSeAusente = null)

    private fun paraDados(disponivelSeAusente: Boolean?): DadosDoItem {
        val disponibilidade = disponivel ?: disponivelSeAusente
        exigirValidos(
            validarItem(nome.orEmpty(), descricao.orEmpty(), precoCentavos) +
                listOfNotNull(ErroDeCampo("disponivel", "é obrigatório").takeIf { disponibilidade == null }),
        )
        return DadosDoItem(
            nome = nome.orEmpty(),
            descricao = descricao.orEmpty(),
            preco = Dinheiro(requireNotNull(precoCentavos)),
            disponivel = requireNotNull(disponibilidade),
        )
    }
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
