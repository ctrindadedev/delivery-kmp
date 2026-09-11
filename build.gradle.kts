plugins {
    alias(libs.plugins.kotlinMultiplatform)  apply false
    alias(libs.plugins.kotlinJvm)           apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ktlint)  apply false
    alias(libs.plugins.detekt) apply false
}

// ktlint e detekt aplicados a shared/ e api/ — os únicos módulos Kotlin.
// services/catalogo/ é Go e não passa por aqui.
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
    }
}
