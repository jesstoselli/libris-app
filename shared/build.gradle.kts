import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "app.billfolder.libris.shared"
        compileSdk = 37
        minSdk = 26
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        optIn.addAll("kotlin.time.ExperimentalTime")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}
