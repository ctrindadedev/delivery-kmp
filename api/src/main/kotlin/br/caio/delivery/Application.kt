package br.caio.delivery

import br.caio.delivery.aplicacao.RepositorioDeItens
import br.caio.delivery.aplicacao.RepositorioDeRestaurantes
import br.caio.delivery.aplicacao.ServicoDeItens
import br.caio.delivery.aplicacao.ServicoDeRestaurantes
import br.caio.delivery.infra.http.configureOpenApi
import br.caio.delivery.infra.http.configureRouting
import br.caio.delivery.infra.http.configureSerialization
import br.caio.delivery.infra.http.configureStatusPages
import br.caio.delivery.infra.persistencia.BancoDeDados
import br.caio.delivery.infra.persistencia.ConfiguracaoDoBanco
import br.caio.delivery.infra.persistencia.RepositorioExposedDeItens
import br.caio.delivery.infra.persistencia.RepositorioExposedDeRestaurantes
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    val banco = BancoDeDados.conectar(ConfiguracaoDoBanco.doAmbiente())
    monitor.subscribe(ApplicationStopped) { banco.close() }

    moduloHttp(
        restaurantes = RepositorioExposedDeRestaurantes(banco.database),
        itens = RepositorioExposedDeItens(banco.database),
    )
}

fun Application.moduloHttp(
    restaurantes: RepositorioDeRestaurantes,
    itens: RepositorioDeItens,
) {
    configureSerialization()
    configureStatusPages()
    configureOpenApi()
    configureRouting(ServicoDeRestaurantes(restaurantes), ServicoDeItens(restaurantes, itens))
}
