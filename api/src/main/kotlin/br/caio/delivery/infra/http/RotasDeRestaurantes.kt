package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.ServicoDeItens
import br.caio.delivery.aplicacao.ServicoDeRestaurantes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.rotasDeRestaurantes(
    restaurantes: ServicoDeRestaurantes,
    itens: ServicoDeItens,
) {
    route("/restaurantes") {
        get {
            val pagina = restaurantes.listar(call.request.queryParameters["nome"], call.pagina())
            call.respond(pagina.paraResposta { it.paraResposta() })
        }

        post {
            val requisicao = call.receive<RestauranteRequisicao>()
            val restaurante = restaurantes.criar(requisicao.nome)
            call.response.header(HttpHeaders.Location, "/restaurantes/${restaurante.id.value}")
            call.respond(HttpStatusCode.Created, restaurante.paraResposta())
        }

        route("/{id}") {
            get {
                call.respond(restaurantes.buscar(call.restauranteId()).paraResposta())
            }

            put {
                val requisicao = call.receive<RestauranteRequisicao>()
                call.respond(restaurantes.atualizar(call.restauranteId(), requisicao.nome).paraResposta())
            }

            delete {
                restaurantes.remover(call.restauranteId())
                call.respond(HttpStatusCode.NoContent)
            }

            route("/itens") {
                rotasDeItens(itens)
            }
        }
    }
}

private fun Route.rotasDeItens(itens: ServicoDeItens) {
    get {
        val pagina = itens.listar(call.restauranteId(), call.pagina())
        call.respond(pagina.paraResposta { it.paraResposta() })
    }

    post {
        val restauranteId = call.restauranteId()
        val requisicao = call.receive<ItemRequisicao>()
        val item = itens.criar(restauranteId, requisicao.paraDados())
        call.response.header(HttpHeaders.Location, "/restaurantes/${restauranteId.value}/itens/${item.id.value}")
        call.respond(HttpStatusCode.Created, item.paraResposta())
    }

    route("/{itemId}") {
        get {
            call.respond(itens.buscar(call.restauranteId(), call.itemId()).paraResposta())
        }

        put {
            val requisicao = call.receive<ItemRequisicao>()
            val item = itens.atualizar(call.restauranteId(), call.itemId(), requisicao.paraDados())
            call.respond(item.paraResposta())
        }

        delete {
            itens.remover(call.restauranteId(), call.itemId())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
