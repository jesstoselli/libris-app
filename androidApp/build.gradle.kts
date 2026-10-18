import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val PRODUCTION_API_URL = "https://libris-api.billfolder.app"

/** `libris.apiUrl=http://10.0.2.2:5090` in local.properties points debug builds at the API running on this Mac (emulator only). */
val localApiUrl: String? = rootProject.file("local.properties").takeIf { it.exists() }
    ?.let { file -> Properties().apply { file.inputStream().use(::load) } }
    ?.getProperty("libris.apiUrl")
    ?.takeIf { it.isNotBlank() }

android {
    namespace = "app.billfolder.libris.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.billfolder.libris"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures { buildConfig = true }

    buildTypes {
        debug {
            // Debug builds run on real phones too, where a local address goes nowhere; the local API is opt-in.
            buildConfigField("String", "API_BASE_URL", "\"${localApiUrl ?: PRODUCTION_API_URL}\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"$PRODUCTION_API_URL\"")
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.serialization.json)
}
