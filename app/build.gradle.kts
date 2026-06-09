plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.nlhd.appperformance"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nlhd.appperformance"
        minSdk = 24
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.navigation:navigation-fragment-ktx:2.9.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.6")

    implementation("androidx.viewpager2:viewpager2:1.1.0")


    implementation("androidx.media3:media3-exoplayer:1.9.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.9.0")
    implementation("androidx.media3:media3-ui:1.9.0")
    implementation("androidx.media3:media3-ui-compose:1.9.0")

    //ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")

    //Ktor
    implementation("io.ktor:ktor-client-core:3.3.3")
    implementation("io.ktor:ktor-client-cio:3.3.3")
    implementation("io.ktor:ktor-client-okhttp:3.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.3")
    implementation("io.ktor:ktor-client-logging:3.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")

    //Hilt
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-compiler:2.57.1")

    //Paging 3
    //Paging
    implementation("androidx.paging:paging-runtime:3.3.6")
    implementation("androidx.paging:paging-compose:3.3.6")

    //Glide
    implementation("com.github.bumptech.glide:glide:5.0.5")

    implementation("androidx.fragment:fragment-ktx:1.8.9")

    //Lottie
    implementation("com.airbnb.android:lottie-compose:6.7.1")

    //DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    //Refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")

    //SplashScreen
    implementation("androidx.core:core-splashscreen:1.2.0")

    // CameraX
    val camerax_version = "1.4.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("androidx.media3:media3-transformer:1.10.1")
    implementation("androidx.media3:media3-effect:1.10.1")
    implementation("androidx.media3:media3-common:1.10.1")

    implementation("jp.co.cyberagent.android:gpuimage:2.1.0")
}