package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.ServicoDeItens
import br.caio.delivery.aplicacao.ServicoDeRestaurantes
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.rotasDeRestaurantes(
    restaurantes: ServicoDeRestaurantes,
    itens: ServicoDeItens,
) {
    route("/restaurantes") {
        get(Documentacao.listarRestaurantes) {
            val pagina = restaurantes.listar(call.request.queryParameters["nome"], call.pagina())
            call.respond(pagina.paraResposta { it.paraResposta() })
        }

        post(Documentacao.criarRestaurante) {
            val nome = call.receive<RestauranteRequisicao>().nomeValido()
            val restaurante = restaurantes.criar(nome)
            call.response.header(HttpHeaders.Location, "/restaurantes/${restaurante.id.value}")
            call.respond(HttpStatusCode.Created, restaurante.paraResposta())
        }

        route("/{id}") {
            get(Documentacao.buscarRestaurante) {
                call.respond(restaurantes.buscar(call.restauranteId()).paraResposta())
            }

            put(Documentacao.atualizarRestaurante) {
                val restauranteId = call.restauranteId()
                val nome = call.receive<RestauranteRequisicao>().nomeValido()
                call.respond(restaurantes.atualizar(restauranteId, nome).paraResposta())
            }

            delete(Documentacao.removerRestaurante) {
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
    get(Documentacao.listarItens) {
        val pagina = itens.listar(call.restauranteId(), call.pagina())
        call.respond(pagina.paraResposta { it.paraResposta() })
    }

    post(Documentacao.criarItem) {
        val restauranteId = call.restauranteId()
        val dados = call.receive<NovoItemRequisicao>().paraDados()
        val item = itens.criar(restauranteId, dados)
        call.response.header(HttpHeaders.Location, "/restaurantes/${restauranteId.value}/itens/${item.id.value}")
        call.respond(HttpStatusCode.Created, item.paraResposta())
    }

    route("/{itemId}") {
        get(Documentacao.buscarItem) {
            call.respond(itens.buscar(call.restauranteId(), call.itemId()).paraResposta())
        }

        put(Documentacao.atualizarItem) {
            val restauranteId = call.restauranteId()
            val itemId = call.itemId()
            val dados = call.receive<ItemRequisicao>().paraDados()
            val item = itens.atualizar(restauranteId, itemId, dados)
            call.respond(item.paraResposta())
        }

        delete(Documentacao.removerItem) {
            itens.remover(call.restauranteId(), call.itemId())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
