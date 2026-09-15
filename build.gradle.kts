plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

val detektVersion = libs.versions.detekt.get()

// As verificações Kotlin são aplicadas aos módulos api, shared e app.
// O serviço catalogo é validado pelas ferramentas do ecossistema Go.
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    }
}

tasks.register("ktlintCheck") {
    group = "verification"
    description = "Executa o ktlint nos módulos Kotlin."
    dependsOn(":api:ktlintCheck", ":shared:ktlintCheck", ":app:ktlintCheck")
}

tasks.register("detekt") {
    group = "verification"
    description = "Executa o detekt nos módulos Kotlin."
    dependsOn(":api:detekt", ":shared:detektMetadataMain", ":app:detekt")
}
