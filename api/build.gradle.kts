plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

application {
    mainClass.set("br.caio.delivery.ApplicationKt")
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.neg)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.status.pages)

    implementation(libs.koin.ktor)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)

    implementation(libs.postgres)
    implementation(libs.logback)
}
