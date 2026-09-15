package br.caio.delivery.dominio

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class RestaurantId(
    val value: Uuid,
) {
    companion object {
        fun novo(): RestaurantId = RestaurantId(Uuid.random())
    }
}

@OptIn(ExperimentalUuidApi::class)
@JvmInline
value class MenuItemId(
    val value: Uuid,
) {
    companion object {
        fun novo(): MenuItemId = MenuItemId(Uuid.random())
    }
}

@JvmInline
value class Dinheiro(
    val centavos: Long,
) {
    init {
        require(centavos >= 0) { "Valor monetário não pode ser negativo: $centavos centavos" }
    }

    operator fun plus(outro: Dinheiro) = Dinheiro(centavos + outro.centavos)

    operator fun minus(outro: Dinheiro) = Dinheiro(centavos - outro.centavos)

    operator fun times(fator: Int) = Dinheiro(centavos * fator)

    operator fun compareTo(outro: Dinheiro) = centavos.compareTo(outro.centavos)

    fun exibir(): String {
        val reais = centavos / CENTAVOS_POR_REAL
        val centavosRestantes = centavos % CENTAVOS_POR_REAL
        return "R$ $reais,${centavosRestantes.toString().padStart(2, '0')}"
    }

    companion object {
        private const val CENTAVOS_POR_REAL = 100L

        fun deReais(
            reais: Long,
            centavos: Long = 0,
        ): Dinheiro = Dinheiro(reais * CENTAVOS_POR_REAL + centavos)
    }
}

data class MenuItem(
    val id: MenuItemId,
    val nome: String,
    val descricao: String,
    val preco: Dinheiro,
    val disponivel: Boolean = true,
)

data class Restaurant(
    val id: RestaurantId,
    val nome: String,
    val itens: List<MenuItem> = emptyList(),
) {
    fun adicionarItem(item: MenuItem): Restaurant {
        require(item.preco.centavos > 0) {
            "Item '${item.nome}' precisa ter preço positivo"
        }
        return copy(itens = itens + item)
    }

    fun removerItem(id: MenuItemId): Restaurant {
        val novaLista = itens.filter { it.id != id }
        require(novaLista.isNotEmpty()) {
            "Restaurante precisa ter ao menos um item no cardápio"
        }
        return copy(itens = novaLista)
    }

    fun atualizarDisponibilidade(
        id: MenuItemId,
        disponivel: Boolean,
    ): Restaurant {
        val novaLista =
            itens.map {
                if (it.id == id) it.copy(disponivel = disponivel) else it
            }
        return copy(itens = novaLista)
    }
}
