package br.caio.delivery.infra.persistencia

data class ConfiguracaoDoBanco(
    val url: String,
    val usuario: String,
    val senha: String,
) {
    override fun toString() = "ConfiguracaoDoBanco(url=$url, usuario=$usuario, senha=***)"

    companion object {
        private const val URL_PADRAO = "jdbc:postgresql://localhost:5432/delivery"
        private const val USUARIO_PADRAO = "delivery"
        private const val SENHA_PADRAO = "delivery"

        fun doAmbiente(ambiente: (String) -> String? = System::getenv) =
            ConfiguracaoDoBanco(
                url = ambiente("DATABASE_URL") ?: URL_PADRAO,
                usuario = ambiente("DATABASE_USER") ?: USUARIO_PADRAO,
                senha = ambiente("DATABASE_PASSWORD") ?: SENHA_PADRAO,
            )
    }
}
