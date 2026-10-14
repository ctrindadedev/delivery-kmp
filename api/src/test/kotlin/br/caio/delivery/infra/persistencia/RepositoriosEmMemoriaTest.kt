package br.caio.delivery.infra.persistencia

class RepositoriosEmMemoriaTest : ContratoDeRepositorios() {
    override fun novosRepositorios() = RepositoriosEmMemoria().let { it.restaurantes to it.itens }
}
