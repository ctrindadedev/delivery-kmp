package br.caio.delivery.aplicacao

sealed class ErroDeAplicacao(
    mensagem: String,
) : RuntimeException(mensagem)

class RecursoNaoEncontrado(
    mensagem: String,
) : ErroDeAplicacao(mensagem)

class ConflitoComEstadoAtual(
    mensagem: String,
) : ErroDeAplicacao(mensagem)

class RegraDeNegocioViolada(
    mensagem: String,
) : ErroDeAplicacao(mensagem)
