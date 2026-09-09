plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    jvmToolchain(libs.versions.java.get().toInt())

    sourceSets {
        commonMain.dependencies {
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
