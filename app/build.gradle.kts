plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.abtahiapp.dontworry"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.abtahiapp.dontworry"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GOOGLE_API_KEY", "\"${project.properties["GOOGLE_API_KEY"]}\"")
        buildConfigField("String", "CUSTOM_SEARCH_ENGINE_ID", "\"${project.properties["CUSTOM_SEARCH_ENGINE_ID"]}\"")
        buildConfigField("String", "NEWS_API_KEY", "\"${project.properties["NEWS_API_KEY"]}\"")
        buildConfigField("String", "TMDB_API_KEY", "\"${project.properties["TMDB_API_KEY"]}\"")
        buildConfigField("String", "QUOTE_API_NINJA_KEY", "\"${project.properties["QUOTE_API_NINJA_KEY"]}\"")
        buildConfigField("String", "OPEN_WEATHER_API_KEY", "\"${project.properties["OPEN_WEATHER_API_KEY"]}\"")
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation (libs.play.services.auth)
    implementation (libs.glide)
    implementation (libs.circleimageview)
    implementation (libs.core)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)
    implementation (libs.exoplayer)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.fragment.ktx)
}