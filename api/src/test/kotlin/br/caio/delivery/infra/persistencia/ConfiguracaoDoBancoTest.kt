package br.caio.delivery.infra.persistencia

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class ConfiguracaoDoBancoTest {
    @Test
    fun `le a conexao das variaveis de ambiente`() {
        val ambiente =
            mapOf(
                "DATABASE_URL" to "jdbc:postgresql://banco:5432/outro",
                "DATABASE_USER" to "usuario",
                "DATABASE_PASSWORD" to "segredo",
            )

        val configuracao = ConfiguracaoDoBanco.doAmbiente(ambiente::get)

        assertEquals(ConfiguracaoDoBanco("jdbc:postgresql://banco:5432/outro", "usuario", "segredo"), configuracao)
    }

    @Test
    fun `usa o banco de desenvolvimento quando o ambiente nao define nada`() {
        val configuracao = ConfiguracaoDoBanco.doAmbiente { null }

        assertEquals("jdbc:postgresql://localhost:5432/delivery", configuracao.url)
        assertEquals("delivery", configuracao.usuario)
    }

    @Test
    fun `nao expoe a senha no toString`() {
        val configuracao = ConfiguracaoDoBanco("jdbc:postgresql://banco/db", "usuario", "segredo")

        assertFalse(configuracao.toString().contains("segredo"))
    }
}
