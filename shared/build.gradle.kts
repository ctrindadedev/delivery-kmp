plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

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

// O task "detekt" padrão só olha src/main/kotlin, que não existe em KMP.
// "detektMetadataMain" é o que de fato analisa commonMain — precisa
// ser plugado manualmente no "check" para não passar batido no CI.
tasks.named("check") {
    dependsOn("detektMetadataMain")
}
