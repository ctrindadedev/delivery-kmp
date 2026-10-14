package br.caio.delivery

import br.caio.delivery.infra.http.configureRouting
import br.caio.delivery.infra.http.configureSerialization
import br.caio.delivery.infra.persistencia.BancoDeDados
import br.caio.delivery.infra.persistencia.ConfiguracaoDoBanco
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

    configureSerialization()
    configureRouting()
}
