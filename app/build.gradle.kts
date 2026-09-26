@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import com.android.build.api.variant.HasUnitTestBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.navigation.compose)
            implementation(libs.material3.window.size)
            implementation(libs.material3.adaptive)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

android {
    namespace = "br.caio.delivery.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.caio.delivery"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

androidComponents {
    beforeVariants { variant ->
        (variant as? HasUnitTestBuilder)?.enableUnitTest = false
    }
}

compose.desktop {
    application {
        mainClass = "br.caio.delivery.app.MainKt"
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/desktopMain/kotlin",
    )
}
