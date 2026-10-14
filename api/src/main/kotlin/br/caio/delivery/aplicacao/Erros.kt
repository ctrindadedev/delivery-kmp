package br.caio.delivery.aplicacao

import br.caio.delivery.dominio.ErroDeCampo

sealed class ErroDeAplicacao(
    mensagem: String,
) : RuntimeException(mensagem)

class RecursoNaoEncontrado(
    mensagem: String,
) : ErroDeAplicacao(mensagem)

class ConflitoComEstadoAtual(
    mensagem: String,
) : ErroDeAplicacao(mensagem)

class DadosInvalidos(
    val erros: List<ErroDeCampo>,
) : ErroDeAplicacao("Dados inválidos: ${erros.joinToString { "${it.campo} ${it.mensagem}" }}")

fun exigirValidos(erros: List<ErroDeCampo>) {
    if (erros.isNotEmpty()) throw DadosInvalidos(erros)
}
