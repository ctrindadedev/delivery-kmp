package br.caio.delivery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.sql.DriverManager

@Testcontainers
class PostgresContainerSmokeTest {
    @Test
    fun `testcontainers sobe um postgres e conecta`() {
        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password).use { conexao ->
            conexao.createStatement().executeQuery("SELECT 1").use { resultado ->
                resultado.next()
                assertEquals(1, resultado.getInt(1))
            }
        }
    }

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }
}
