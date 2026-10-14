package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.DadosInvalidos
import br.caio.delivery.aplicacao.Pagina
import br.caio.delivery.dominio.ErroDeCampo
import br.caio.delivery.dominio.MenuItemId
import br.caio.delivery.dominio.RestaurantId
import io.ktor.server.application.ApplicationCall
import kotlin.uuid.Uuid

fun ApplicationCall.restauranteId() = RestaurantId(uuidDoCaminho("id"))

fun ApplicationCall.itemId() = MenuItemId(uuidDoCaminho("itemId"))

fun ApplicationCall.pagina() =
    Pagina(
        numero = inteiroDaConsulta("pagina") ?: 1,
        tamanho = inteiroDaConsulta("tamanho") ?: Pagina.TAMANHO_PADRAO,
    )

private fun ApplicationCall.uuidDoCaminho(nome: String): Uuid =
    parameters[nome]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
        ?: throw DadosInvalidos(listOf(ErroDeCampo(nome, "deve ser um UUID válido")))

private fun ApplicationCall.inteiroDaConsulta(nome: String): Int? {
    val valor = request.queryParameters[nome] ?: return null
    return valor.toIntOrNull() ?: throw DadosInvalidos(listOf(ErroDeCampo(nome, "deve ser um número inteiro")))
}
