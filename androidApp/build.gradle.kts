plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

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
            // The emulator reaches the Mac's localhost through 10.0.2.2.
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5090\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://libris-api.billfolder.app\"")
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
