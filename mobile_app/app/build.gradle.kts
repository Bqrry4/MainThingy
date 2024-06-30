plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    //kotlin("kapt")
    id("kotlin-parcelize")

}

android {
    namespace = "com.nyanthingy.mobileapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nyanthingy.mobileapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    //Jetpack Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.util)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.preference)
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    ksp(libs.hilt.android.compiler)

    // Google maps
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.maps.compose)
    implementation(libs.maps.ktx)
    implementation(libs.maps.utils.ktx)
    // OpenStreetMaps
    implementation(libs.osmdroid.android)

    //Ble
    implementation(libs.scanner)
    implementation(libs.client)

    //Permission
    implementation(libs.google.accompanist.permissions)

    //Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    //Coil
    implementation(libs.coil.compose)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    //add kotlinx-serialization
    implementation(libs.kotlinx.serialization.json)

    //Charts
//    implementation(libs.ycharts)

    //Animation
    runtimeOnly("androidx.compose.animation:animation:1.6.7")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    //Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

//// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}