package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.ConflitoComEstadoAtual
import br.caio.delivery.aplicacao.DadosInvalidos
import br.caio.delivery.aplicacao.RecursoNaoEncontrado
import br.caio.delivery.dominio.ErroDeCampo
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.exceptions.ExposedSQLException

@Serializable
data class CampoInvalido(
    val campo: String,
    val mensagem: String,
)

@Serializable
data class ProblemDetails(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String? = null,
    val erros: List<CampoInvalido>? = null,
)

enum class TipoDeProblema(
    val status: HttpStatusCode,
    val titulo: String,
) {
    ENTRADA_INVALIDA(HttpStatusCode.BadRequest, "Entrada inválida"),
    CORPO_INVALIDO(HttpStatusCode.BadRequest, "Corpo da requisição inválido"),
    RECURSO_NAO_ENCONTRADO(HttpStatusCode.NotFound, "Recurso não encontrado"),
    METODO_NAO_PERMITIDO(HttpStatusCode.MethodNotAllowed, "Método não permitido"),
    CONFLITO(HttpStatusCode.Conflict, "Conflito com o estado atual"),
    TIPO_DE_CONTEUDO_NAO_SUPORTADO(HttpStatusCode.UnsupportedMediaType, "Tipo de conteúdo não suportado"),
    ERRO_INTERNO(HttpStatusCode.InternalServerError, "Erro interno"),
    ;

    val uri = "/problemas/" + name.lowercase().replace('_', '-')
}

private const val CLASSE_DE_VIOLACAO_DE_INTEGRIDADE = "23"

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<DadosInvalidos> { call, erro ->
            call.responderCamposInvalidos(erro.erros)
        }
        exception<MissingRequestParameterException> { call, erro ->
            call.responderCamposInvalidos(listOf(ErroDeCampo(erro.parameterName, "é obrigatório")))
        }
        exception<ParameterConversionException> { call, erro ->
            call.responderCamposInvalidos(listOf(ErroDeCampo(erro.parameterName, "tem formato inválido")))
        }
        exception<BadRequestException> { call, erro ->
            call.application.environment.log
                .debug("Corpo da requisição recusado", erro)
            call.responderProblema(TipoDeProblema.CORPO_INVALIDO, "O corpo deve ser um JSON válido com campos do tipo esperado")
        }
        exception<RecursoNaoEncontrado> { call, erro ->
            call.responderProblema(TipoDeProblema.RECURSO_NAO_ENCONTRADO, erro.message.orEmpty())
        }
        exception<ConflitoComEstadoAtual> { call, erro ->
            call.responderProblema(TipoDeProblema.CONFLITO, erro.message.orEmpty())
        }
        exception<ExposedSQLException> { call, erro ->
            if (erro.sqlState.orEmpty().startsWith(CLASSE_DE_VIOLACAO_DE_INTEGRIDADE)) {
                call.application.environment.log
                    .warn("Violação de integridade não tratada no caso de uso", erro)
                call.responderProblema(TipoDeProblema.CONFLITO, "A operação viola uma restrição dos dados")
            } else {
                call.responderErroInterno(erro)
            }
        }
        exception<Throwable> { call, erro ->
            call.responderErroInterno(erro)
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.responderProblema(TipoDeProblema.RECURSO_NAO_ENCONTRADO, "Nenhuma rota atende ${call.request.path()}")
        }
        status(HttpStatusCode.UnsupportedMediaType) { call, _ ->
            call.responderProblema(TipoDeProblema.TIPO_DE_CONTEUDO_NAO_SUPORTADO, "Envie o corpo como application/json")
        }
        status(HttpStatusCode.MethodNotAllowed) { call, _ ->
            call.responderProblema(TipoDeProblema.METODO_NAO_PERMITIDO, "O método não é aceito em ${call.request.path()}")
        }
    }
}

private suspend fun ApplicationCall.responderCamposInvalidos(erros: List<ErroDeCampo>) =
    responderProblema(
        TipoDeProblema.ENTRADA_INVALIDA,
        "A requisição tem campos inválidos",
        erros.map { CampoInvalido(it.campo, it.mensagem) },
    )

private suspend fun ApplicationCall.responderErroInterno(erro: Throwable) {
    application.environment.log.error("Erro não tratado em ${request.path()}", erro)
    responderProblema(TipoDeProblema.ERRO_INTERNO, "Ocorreu um erro inesperado no servidor")
}

private suspend fun ApplicationCall.responderProblema(
    tipo: TipoDeProblema,
    detalhe: String,
    erros: List<CampoInvalido>? = null,
) {
    val problema =
        ProblemDetails(
            type = tipo.uri,
            title = tipo.titulo,
            status = tipo.status.value,
            detail = detalhe,
            instance = request.path(),
            erros = erros,
        )
    respondText(jsonDaApi.encodeToString(ProblemDetails.serializer(), problema), ContentType.Application.ProblemJson, tipo.status)
}
