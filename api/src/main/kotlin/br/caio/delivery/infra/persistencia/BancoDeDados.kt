package br.caio.delivery.infra.persistencia

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

class BancoDeDados private constructor(
    private val fonteDeDados: HikariDataSource,
) : AutoCloseable {
    val database: Database = Database.connect(fonteDeDados)

    override fun close() = fonteDeDados.close()

    companion object {
        fun conectar(configuracao: ConfiguracaoDoBanco): BancoDeDados {
            val fonteDeDados =
                HikariDataSource(
                    HikariConfig().apply {
                        jdbcUrl = configuracao.url
                        username = configuracao.usuario
                        password = configuracao.senha
                    },
                )
            migrar(fonteDeDados)
            return BancoDeDados(fonteDeDados)
        }

        fun migrar(fonteDeDados: DataSource) {
            Flyway
                .configure()
                .dataSource(fonteDeDados)
                .load()
                .migrate()
        }
    }
}
