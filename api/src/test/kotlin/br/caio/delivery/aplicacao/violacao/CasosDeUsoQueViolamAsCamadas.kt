package br.caio.delivery.aplicacao.violacao

import br.caio.delivery.infra.persistencia.RepositoriosEmMemoria
import io.ktor.http.HttpStatusCode

class CasoDeUsoQueConheceOFramework {
    fun responder(): HttpStatusCode = HttpStatusCode.OK
}

class CasoDeUsoQueConheceAInfra {
    fun repositorios(): RepositoriosEmMemoria = RepositoriosEmMemoria()
}
