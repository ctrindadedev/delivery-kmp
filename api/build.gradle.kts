plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )
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

    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(libs.hikaricp)

    implementation(libs.postgres)
    implementation(libs.logback)

    testImplementation(platform(libs.junit.bom))
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
}

tasks.test {
    useJUnitPlatform()
}
