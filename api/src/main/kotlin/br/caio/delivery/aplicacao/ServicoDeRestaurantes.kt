package br.caio.delivery.aplicacao

import br.caio.delivery.dominio.Restaurant
import br.caio.delivery.dominio.RestaurantId

class ServicoDeRestaurantes(
    private val restaurantes: RepositorioDeRestaurantes,
) {
    suspend fun listar(
        nome: String?,
        pagina: Pagina,
    ): PaginaDeResultados<Restaurant> = restaurantes.listar(nome?.takeIf { it.isNotBlank() }, pagina)

    suspend fun buscar(id: RestaurantId): Restaurant = restaurantes.buscarPorId(id) ?: throw restauranteNaoEncontrado(id)

    suspend fun criar(nome: String): Restaurant {
        val restaurante = Restaurant(id = RestaurantId.novo(), nome = nome)
        restaurantes.criar(restaurante)
        return restaurante
    }

    suspend fun atualizar(
        id: RestaurantId,
        nome: String,
    ): Restaurant {
        val atualizado = Restaurant(id = id, nome = nome)
        if (!restaurantes.atualizar(atualizado)) throw restauranteNaoEncontrado(id)
        return atualizado
    }

    suspend fun remover(id: RestaurantId) {
        if (!restaurantes.remover(id)) throw restauranteNaoEncontrado(id)
    }
}

internal fun restauranteNaoEncontrado(id: RestaurantId) = RecursoNaoEncontrado("Restaurante ${id.value} não encontrado")
