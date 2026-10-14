package br.caio.delivery.infra.persistencia

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.sql.Connection
import java.sql.SQLException
import java.util.UUID

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MigracaoTest {
    private val fonteDeDados by lazy {
        PGSimpleDataSource().apply {
            setURL(postgres.jdbcUrl)
            user = postgres.username
            password = postgres.password
        }
    }

    @BeforeAll
    fun migrar() {
        BancoDeDados.migrar(fonteDeDados)
    }

    @Test
    fun `aplica a migracao inicial uma unica vez`() {
        BancoDeDados.migrar(fonteDeDados)

        val versoes = consultar("SELECT version FROM flyway_schema_history WHERE success")

        assertEquals(listOf("1"), versoes)
    }

    @Test
    fun `cria o indice de itens por restaurante`() {
        val indices = consultar("SELECT indexname FROM pg_indexes WHERE tablename = 'item_cardapio'")

        assertTrue("idx_item_cardapio_restaurante_id" in indices)
    }

    @Test
    fun `recusa item com preco zero`() {
        val restaurante = inserirRestaurante()

        val erro = assertThrows<SQLException> { inserirItem(restaurante, precoCentavos = 0) }

        assertEquals(VIOLACAO_DE_CHECK, erro.sqlState)
    }

    @Test
    fun `recusa item de restaurante inexistente`() {
        val erro = assertThrows<SQLException> { inserirItem(UUID.randomUUID(), precoCentavos = 1000) }

        assertEquals(VIOLACAO_DE_CHAVE_ESTRANGEIRA, erro.sqlState)
    }

    @Test
    fun `item nasce disponivel e some junto com o restaurante`() {
        val restaurante = inserirRestaurante()
        val item = inserirItem(restaurante, precoCentavos = 1000)

        assertEquals(listOf("t"), consultar("SELECT disponivel FROM item_cardapio WHERE id = '$item'"))

        executar("DELETE FROM restaurante WHERE id = '$restaurante'")

        assertEquals(emptyList<String>(), consultar("SELECT id FROM item_cardapio WHERE id = '$item'"))
    }

    private fun inserirRestaurante(): UUID =
        UUID.randomUUID().also { id ->
            executar("INSERT INTO restaurante (id, nome) VALUES ('$id', 'Cantina')")
        }

    private fun inserirItem(
        restaurante: UUID,
        precoCentavos: Long,
    ): UUID =
        UUID.randomUUID().also { id ->
            executar(
                "INSERT INTO item_cardapio (id, restaurante_id, nome, preco_centavos) " +
                    "VALUES ('$id', '$restaurante', 'Lasanha', $precoCentavos)",
            )
        }

    private fun executar(sql: String) {
        comConexao { it.createStatement().use { comando -> comando.execute(sql) } }
    }

    private fun consultar(sql: String): List<String> =
        comConexao { conexao ->
            conexao.createStatement().use { comando ->
                comando.executeQuery(sql).use { resultado ->
                    buildList { while (resultado.next()) add(resultado.getString(1)) }
                }
            }
        }

    private fun <T> comConexao(bloco: (Connection) -> T): T = fonteDeDados.connection.use(bloco)

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")

        private const val VIOLACAO_DE_CHECK = "23514"
        private const val VIOLACAO_DE_CHAVE_ESTRANGEIRA = "23503"
    }
}
