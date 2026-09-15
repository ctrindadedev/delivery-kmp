plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvm()
    androidTarget()

    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )

    sourceSets {
        commonMain.dependencies {
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "br.caio.delivery.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}

// O task detekt padrão não cobre commonMain em módulos multiplataforma.
tasks.named("check") {
    dependsOn("detektMetadataMain")
}
