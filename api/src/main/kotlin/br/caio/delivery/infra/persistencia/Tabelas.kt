package br.caio.delivery.infra.persistencia

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object TabelaRestaurante : Table("restaurante") {
    val id = uuid("id")
    val nome = text("nome")

    override val primaryKey = PrimaryKey(id)
}

object TabelaItemCardapio : Table("item_cardapio") {
    val id = uuid("id")
    val restauranteId = uuid("restaurante_id").references(TabelaRestaurante.id, onDelete = ReferenceOption.CASCADE)
    val nome = text("nome")
    val descricao = text("descricao").nullable()
    val precoCentavos = long("preco_centavos")
    val disponivel = bool("disponivel")

    override val primaryKey = PrimaryKey(id)
}
