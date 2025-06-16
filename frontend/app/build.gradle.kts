plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0-RC2" // For Kotlin serialization
    id("com.google.gms.google-services") // Google services plugin for Firebase
}

android {
    namespace = "com.example.frontend"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.frontend"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Compose Bill of Materials
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.6.7") // Extended Material Icons

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Compose BOM for testing
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.0") // Jetpack Navigation for Compose

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.8.1") // For JSON serialization/deserialization

    // Retrofit for network requests
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0") // Gson converter for Retrofit
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0") // Scalars converter for Retrofit (e.g., for plain text responses)

    // Coroutines for asynchronous programming
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Firebase Bill of Materials (BoM) - Manages versions of Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:33.15.0")) // Use the latest compatible version

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx") // Firebase Authentication library (Kotlin extensions)

    // Google Sign-In (required for Firebase Google Sign-In)
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Google Sign-In services, use the latest version

    // Credential Manager (Alternative Google Sign-In method, may be redundant if solely using Firebase Auth for Google Sign-In)
    // If you are using Firebase Authentication for Google Sign-In, these might not be strictly necessary
    // for that specific flow, as firebase-auth and play-services-auth handle it.
    // However, they can be used for other credential management features.
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2") // Bridges Credential Manager with Play Services
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0") // Google Identity Services library
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
}
