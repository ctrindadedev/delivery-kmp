package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.ServicoDeItens
import br.caio.delivery.aplicacao.ServicoDeRestaurantes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(
    restaurantes: ServicoDeRestaurantes,
    itens: ServicoDeItens,
) {
    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }
        rotasDeRestaurantes(restaurantes, itens)
    }
}
