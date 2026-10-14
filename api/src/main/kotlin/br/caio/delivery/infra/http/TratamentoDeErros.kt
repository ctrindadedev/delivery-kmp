package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.ConflitoComEstadoAtual
import br.caio.delivery.aplicacao.RecursoNaoEncontrado
import br.caio.delivery.aplicacao.RegraDeNegocioViolada
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<RecursoNaoEncontrado> { call, erro ->
            call.respond(HttpStatusCode.NotFound, ErroResposta(erro.message.orEmpty()))
        }
        exception<ConflitoComEstadoAtual> { call, erro ->
            call.respond(HttpStatusCode.Conflict, ErroResposta(erro.message.orEmpty()))
        }
        exception<RegraDeNegocioViolada> { call, erro ->
            call.respond(HttpStatusCode.UnprocessableEntity, ErroResposta(erro.message.orEmpty()))
        }
        exception<BadRequestException> { call, erro ->
            call.respond(HttpStatusCode.BadRequest, ErroResposta(erro.message.orEmpty()))
        }
        exception<IllegalArgumentException> { call, erro ->
            call.respond(HttpStatusCode.BadRequest, ErroResposta(erro.message.orEmpty()))
        }
    }
}
