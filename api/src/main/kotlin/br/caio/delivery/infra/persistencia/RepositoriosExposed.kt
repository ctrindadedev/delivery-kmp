package br.caio.delivery.infra.persistencia

import br.caio.delivery.aplicacao.ConflitoComEstadoAtual
import br.caio.delivery.aplicacao.Pagina
import br.caio.delivery.aplicacao.PaginaDeResultados
import br.caio.delivery.aplicacao.RepositorioDeItens
import br.caio.delivery.aplicacao.RepositorioDeRestaurantes
import br.caio.delivery.aplicacao.restauranteNaoEncontrado
import br.caio.delivery.dominio.MenuItem
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.LikePattern
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class RepositorioExposedDeRestaurantes(
    private val database: Database,
) : RepositorioDeRestaurantes {
    override suspend fun listar(
        nome: String?,
        pagina: Pagina,
    ) = database.suspendTransaction {
        val consulta = TabelaRestaurante.selectAll()
        if (nome != null) {
            consulta.where { TabelaRestaurante.nome.lowerCase() like LikePattern.ofLiteral(nome.lowercase()).contido() }
        }
        consulta
            .orderBy(TabelaRestaurante.nome.lowerCase() to SortOrder.ASC, TabelaRestaurante.id to SortOrder.ASC)
            .paginar(pagina) { it.paraRestaurante() }
    }

    override suspend fun buscarPorId(id: RestaurantId) =
        database.suspendTransaction {
            TabelaRestaurante
                .selectAll()
                .where { TabelaRestaurante.id eq id.paraColuna() }
                .singleOrNull()
                ?.let { linha ->
                    val itens =
                        TabelaItemCardapio
                            .selectAll()
                            .where { TabelaItemCardapio.restauranteId eq id.paraColuna() }
                            .ordemDoCardapio()
                            .map { it.paraItem() }
                    linha.paraRestaurante(itens)
                }
        }

    override suspend fun existe(id: RestaurantId) =
        database.suspendTransaction {
            !TabelaRestaurante.selectAll().where { TabelaRestaurante.id eq id.paraColuna() }.empty()
        }

    override suspend fun criar(restaurante: Restaurant) {
        comNomeUnico(restaurante) {
            database.suspendTransaction {
                TabelaRestaurante.insert { it.inserir(restaurante) }
            }
        }
    }

    override suspend fun atualizar(restaurante: Restaurant) =
        comNomeUnico(restaurante) {
            database.suspendTransaction {
                TabelaRestaurante.update({ TabelaRestaurante.id eq restaurante.id.paraColuna() }) {
                    it.preencher(restaurante)
                } > 0
            }
        }

    override suspend fun remover(id: RestaurantId) =
        database.suspendTransaction {
            TabelaRestaurante.deleteWhere { TabelaRestaurante.id eq id.paraColuna() } > 0
        }
}

class RepositorioExposedDeItens(
    private val database: Database,
) : RepositorioDeItens {
    override suspend fun listar(
        restauranteId: RestaurantId,
        pagina: Pagina,
    ) = database.suspendTransaction {
        TabelaItemCardapio
            .selectAll()
            .where { TabelaItemCardapio.restauranteId eq restauranteId.paraColuna() }
            .ordemDoCardapio()
            .paginar(pagina) { it.paraItem() }
    }

    override suspend fun buscarPorId(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ) = database.suspendTransaction {
        TabelaItemCardapio
            .selectAll()
            .where { doRestaurante(restauranteId, itemId) }
            .singleOrNull()
            ?.paraItem()
    }

    override suspend fun criar(
        restauranteId: RestaurantId,
        item: MenuItem,
    ) {
        database.suspendTransaction {
            try {
                TabelaItemCardapio.insert { it.inserir(restauranteId, item) }
            } catch (erro: ExposedSQLException) {
                if (erro.sqlState != VIOLACAO_DE_CHAVE_ESTRANGEIRA) throw erro
                throw restauranteNaoEncontrado(restauranteId)
            }
        }
    }

    override suspend fun atualizar(
        restauranteId: RestaurantId,
        item: MenuItem,
    ) = database.suspendTransaction {
        TabelaItemCardapio.update({ doRestaurante(restauranteId, item.id) }) { it.preencher(item) } > 0
    }

    override suspend fun remover(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ) = database.suspendTransaction {
        TabelaItemCardapio.deleteWhere { doRestaurante(restauranteId, itemId) } > 0
    }

    private fun doRestaurante(
        restauranteId: RestaurantId,
        itemId: MenuItemId,
    ) = (TabelaItemCardapio.id eq itemId.paraColuna()) and
        (TabelaItemCardapio.restauranteId eq restauranteId.paraColuna())
}

private const val VIOLACAO_DE_UNICIDADE = "23505"
private const val VIOLACAO_DE_CHAVE_ESTRANGEIRA = "23503"

private fun Query.ordemDoCardapio() = orderBy(TabelaItemCardapio.nome.lowerCase() to SortOrder.ASC, TabelaItemCardapio.id to SortOrder.ASC)

private inline fun <T> comNomeUnico(
    restaurante: Restaurant,
    operacao: () -> T,
): T =
    try {
        operacao()
    } catch (erro: ExposedSQLException) {
        if (erro.sqlState != VIOLACAO_DE_UNICIDADE) throw erro
        throw ConflitoComEstadoAtual("Já existe um restaurante com o nome '${restaurante.nome}'")
    }

private fun LikePattern.contido() = LikePattern("%$pattern%", escapeChar)

private fun <T> Query.paginar(
    pagina: Pagina,
    paraDominio: (ResultRow) -> T,
): PaginaDeResultados<T> {
    val total = copy().count()
    val itens = limit(pagina.tamanho).offset(pagina.deslocamento).map(paraDominio)
    return PaginaDeResultados(itens, pagina, total)
}
