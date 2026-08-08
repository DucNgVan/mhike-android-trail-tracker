plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.hiking_application_coursework"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.hiking_application_coursework"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    
    // Mapbox SDK - Dùng phiên bản NDK27 để hỗ trợ kích thước trang 16 KB (Fix lỗi tương thích)
    implementation("com.mapbox.maps:android-ndk27:11.11.0")
    
    // Google Play Services for Location
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    
    // Retrofit & Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
