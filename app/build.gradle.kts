plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp") version "2.0.21-1.0.25" // A versão deve bater com a do seu Kotlin

}

android {
    namespace = "com.openinventory.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.openinventory.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SCAN_ACTION", "\"com.openinventory.app.scan\"")
        manifestPlaceholders["scanAction"] = "com.openinventory.app.scan"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.room3.common.jvm)
    val room_version = "2.6.1"
    val cameraxVersion = "1.3.1"
    // Core do Room
    implementation("androidx.room:room-runtime:$room_version")

    // Suporte para Coroutines (suspend functions) e Flow
    implementation("androidx.room:room-ktx:$room_version")

    // Processador de anotações (O "cérebro" que gera o código)
    ksp("androidx.room:room-compiler:$room_version")

    // Opcional: Para testar o banco de dados
    testImplementation("androidx.room:room-testing:$room_version")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0") // pedir permissao da camera
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}