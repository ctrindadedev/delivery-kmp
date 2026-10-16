package br.caio.delivery.infra.http

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.ExampleEncoder
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

const val CAMINHO_DA_ESPECIFICACAO = "/openapi.json"
const val CAMINHO_DA_SWAGGER_UI = "/swagger"

fun Application.configureOpenApi() {
    install(OpenApi) {
        info {
            title = "Delivery KMP"
            version = "1.0.0"
            description = "API de restaurantes e cardápios do projeto Delivery KMP. Erros seguem a RFC 9457 (problem details)."
        }
        schemas {
            generator =
                SchemaGenerator.kotlinx(jsonDaApi) {
                    overwrite(SchemaGenerator.TypeOverwrites.KotlinUuid())
                    referencePath = RefType.OPENAPI_SIMPLE
                }
        }
        examples {
            exampleEncoder = ExampleEncoder.kotlinx(jsonDaApi)
        }
    }
}

fun Route.rotasDeDocumentacao() {
    route(CAMINHO_DA_ESPECIFICACAO) {
        openApi()
    }
    route(CAMINHO_DA_SWAGGER_UI) {
        swaggerUI(CAMINHO_DA_ESPECIFICACAO)
    }
}
