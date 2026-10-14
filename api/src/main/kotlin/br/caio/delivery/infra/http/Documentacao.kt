package br.caio.delivery.infra.http

import br.caio.delivery.aplicacao.Pagina
import br.caio.delivery.dominio.DESCRICAO_TAMANHO_MAXIMO
import br.caio.delivery.dominio.NOME_TAMANHO_MAXIMO
import io.github.smiley4.ktoropenapi.config.RequestConfig
import io.github.smiley4.ktoropenapi.config.ResponsesConfig
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.uuid.Uuid

private const val TAG_RESTAURANTES = "Restaurantes"
private const val TAG_ITENS = "Itens do cardápio"

private val exemploDeRestaurante = RestauranteRequisicao(nome = "Cantina da Nonna")

private val exemploDeItem =
    NovoItemRequisicao(nome = "Lasanha à bolonhesa", descricao = "Massa fresca e molho de tomate", precoCentavos = 4500, disponivel = true)

object Documentacao {
    val saude: RouteConfig.() -> Unit = {
        operationId = "verificarSaude"
        summary = "Verifica se a API está no ar"
        tags("Infraestrutura")
        response {
            code(HttpStatusCode.OK) { description = "A API está respondendo" }
        }
    }

    val listarRestaurantes: RouteConfig.() -> Unit = {
        operationId = "listarRestaurantes"
        summary = "Lista restaurantes, com filtro por nome e paginação"
        tags(TAG_RESTAURANTES)
        request {
            queryParameter<String>("nome") {
                description = "Trecho do nome, sem diferenciar maiúsculas"
                required = false
            }
            paginacao()
        }
        response {
            code(HttpStatusCode.OK) {
                description = "Página de restaurantes, ordenada por nome"
                body<PaginaResposta<RestauranteResposta>>()
            }
            problemas(entradaInvalida = "Parâmetro de paginação inválido")
        }
    }

    val criarRestaurante: RouteConfig.() -> Unit = {
        operationId = "criarRestaurante"
        summary = "Cadastra um restaurante"
        tags(TAG_RESTAURANTES)
        request {
            corpoDeRestaurante()
        }
        response {
            criado<RestauranteResposta>("Restaurante criado", "/restaurantes/{id}")
            problemas(
                entradaInvalida = "Nome ausente, em branco ou longo demais, ou JSON malformado",
                conflito = "Já existe um restaurante com esse nome",
                comCorpo = true,
            )
        }
    }

    val buscarRestaurante: RouteConfig.() -> Unit = {
        operationId = "buscarRestaurante"
        summary = "Busca um restaurante pelo id"
        tags(TAG_RESTAURANTES)
        request {
            idDoRestaurante()
        }
        response {
            code(HttpStatusCode.OK) {
                description = "Restaurante encontrado"
                body<RestauranteResposta>()
            }
            problemas(entradaInvalida = "Id não é um UUID", naoEncontrado = "Restaurante não existe")
        }
    }

    val atualizarRestaurante: RouteConfig.() -> Unit = {
        operationId = "atualizarRestaurante"
        summary = "Atualiza o nome de um restaurante"
        tags(TAG_RESTAURANTES)
        request {
            idDoRestaurante()
            corpoDeRestaurante()
        }
        response {
            code(HttpStatusCode.OK) {
                description = "Restaurante atualizado"
                body<RestauranteResposta>()
            }
            problemas(
                entradaInvalida = "Id não é um UUID, ou nome inválido",
                naoEncontrado = "Restaurante não existe",
                conflito = "Já existe outro restaurante com esse nome",
                comCorpo = true,
            )
        }
    }

    val removerRestaurante: RouteConfig.() -> Unit = {
        operationId = "removerRestaurante"
        summary = "Remove um restaurante e todo o seu cardápio"
        tags(TAG_RESTAURANTES)
        request {
            idDoRestaurante()
        }
        response {
            code(HttpStatusCode.NoContent) { description = "Restaurante removido" }
            problemas(entradaInvalida = "Id não é um UUID", naoEncontrado = "Restaurante não existe")
        }
    }

    val listarItens: RouteConfig.() -> Unit = {
        operationId = "listarItens"
        summary = "Lista o cardápio de um restaurante, com paginação"
        tags(TAG_ITENS)
        request {
            idDoRestaurante()
            paginacao()
        }
        response {
            code(HttpStatusCode.OK) {
                description = "Página de itens, ordenada por nome"
                body<PaginaResposta<ItemResposta>>()
            }
            problemas(entradaInvalida = "Id ou paginação inválidos", naoEncontrado = "Restaurante não existe")
        }
    }

    val criarItem: RouteConfig.() -> Unit = {
        operationId = "criarItem"
        summary = "Adiciona um item ao cardápio"
        tags(TAG_ITENS)
        request {
            idDoRestaurante()
            corpoDeItem(exemploDeItem)
        }
        response {
            criado<ItemResposta>("Item criado", "/restaurantes/{id}/itens/{itemId}")
            problemas(
                entradaInvalida = "Campos ausentes ou inválidos (por exemplo, preço menor ou igual a zero)",
                naoEncontrado = "Restaurante não existe",
                conflito = "A operação viola uma restrição dos dados",
                comCorpo = true,
            )
        }
    }

    val buscarItem: RouteConfig.() -> Unit = {
        operationId = "buscarItem"
        summary = "Busca um item do cardápio"
        tags(TAG_ITENS)
        request {
            idDoRestaurante()
            idDoItem()
        }
        response {
            code(HttpStatusCode.OK) {
                description = "Item encontrado"
                body<ItemResposta>()
            }
            problemas(entradaInvalida = "Id não é um UUID", naoEncontrado = "Restaurante ou item não existe")
        }
    }

    val atualizarItem: RouteConfig.() -> Unit = {
        operationId = "atualizarItem"
        summary = "Substitui os dados de um item do cardápio"
        tags(TAG_ITENS)
        request {
            idDoRestaurante()
            idDoItem()
            corpoDeItem(exemploDeItem.run { ItemRequisicao(nome, descricao, precoCentavos, disponivel = false) })
        }
        response {
            code(HttpStatusCode.OK) {
                description = "Item atualizado"
                body<ItemResposta>()
            }
            problemas(
                entradaInvalida = "Id não é um UUID, ou campos ausentes ou inválidos",
                naoEncontrado = "Restaurante ou item não existe",
                conflito = "A operação viola uma restrição dos dados",
                comCorpo = true,
            )
        }
    }

    val removerItem: RouteConfig.() -> Unit = {
        operationId = "removerItem"
        summary = "Remove um item do cardápio"
        tags(TAG_ITENS)
        request {
            idDoRestaurante()
            idDoItem()
        }
        response {
            code(HttpStatusCode.NoContent) { description = "Item removido" }
            problemas(entradaInvalida = "Id não é um UUID", naoEncontrado = "Restaurante ou item não existe")
        }
    }
}

private fun RequestConfig.idDoRestaurante() {
    pathParameter<Uuid>("id") { description = "Identificador do restaurante" }
}

private fun RequestConfig.idDoItem() {
    pathParameter<Uuid>("itemId") { description = "Identificador do item do cardápio" }
}

private fun RequestConfig.paginacao() {
    queryParameter<Int>("pagina") {
        description = "Número da página, a partir de 1 (padrão 1)"
        required = false
    }
    queryParameter<Int>("tamanho") {
        description = "Itens por página, de 1 a ${Pagina.TAMANHO_MAXIMO} (padrão ${Pagina.TAMANHO_PADRAO})"
        required = false
    }
}

private fun RequestConfig.corpoDeRestaurante() {
    body<RestauranteRequisicao> {
        required = true
        description = "`nome` tem até $NOME_TAMANHO_MAXIMO caracteres; espaços nas pontas são descartados"
        example("Restaurante") { value = exemploDeRestaurante }
    }
}

private inline fun <reified T> RequestConfig.corpoDeItem(exemplo: T) {
    body<T> {
        required = true
        description =
            "`nome` tem até $NOME_TAMANHO_MAXIMO caracteres, `descricao` até $DESCRICAO_TAMANHO_MAXIMO " +
            "e `precoCentavos` deve ser maior que zero"
        example("Item") { value = exemplo }
    }
}

private inline fun <reified T> ResponsesConfig.criado(
    descricao: String,
    localizacao: String,
) {
    code(HttpStatusCode.Created) {
        description = descricao
        header<String>(HttpHeaders.Location) { description = "Caminho do recurso criado, no formato $localizacao" }
        body<T>()
    }
}

private fun ResponsesConfig.problemas(
    entradaInvalida: String,
    naoEncontrado: String? = null,
    conflito: String? = null,
    comCorpo: Boolean = false,
) {
    problema(HttpStatusCode.BadRequest, entradaInvalida)
    naoEncontrado?.let { problema(HttpStatusCode.NotFound, it) }
    conflito?.let { problema(HttpStatusCode.Conflict, it) }
    if (comCorpo) problema(HttpStatusCode.UnsupportedMediaType, "O corpo não foi enviado como application/json")
    problema(HttpStatusCode.InternalServerError, "Erro inesperado no servidor")
}

private fun ResponsesConfig.problema(
    status: HttpStatusCode,
    descricao: String,
) {
    code(status) {
        description = descricao
        body<ProblemDetails> {
            mediaTypes(ContentType.Application.ProblemJson)
        }
    }
}
