package br.caio.delivery.infra.persistencia

import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoriosExposedTest : ContratoDeRepositorios() {
    private lateinit var banco: BancoDeDados

    @BeforeAll
    fun conectar() {
        banco = BancoDeDados.conectar(ConfiguracaoDoBanco(postgres.jdbcUrl, postgres.username, postgres.password))
    }

    @AfterAll
    fun desconectar() {
        banco.close()
    }

    override fun novosRepositorios(): Pair<RepositorioExposedDeRestaurantes, RepositorioExposedDeItens> {
        transaction(banco.database) {
            TabelaItemCardapio.deleteAll()
            TabelaRestaurante.deleteAll()
        }
        return RepositorioExposedDeRestaurantes(banco.database) to RepositorioExposedDeItens(banco.database)
    }

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }
}
