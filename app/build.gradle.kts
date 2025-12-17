plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"
}




android {
    namespace = "ph.edu.auf.thalia.hingpit.outdooractivityplanner"
    compileSdk = 34




    defaultConfig {
        applicationId = "ph.edu.auf.thalia.hingpit.outdooractivityplanner"
        minSdk = 24
        targetSdk = 34
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
        compose = true
    }




    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}




dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)




    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)




    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)




    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")




    // Glide
    implementation(libs.glide)




    //Coil
    implementation("io.coil-kt:coil-compose:2.5.0")




    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)




    // Location Services
    implementation(libs.play.services.location)




    // Permission Handling
    implementation(libs.accompanist.permissions)


    //Firebase
    implementation("com.google.firebase:firebase-analytics-ktx:22.1.2")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    implementation("com.google.firebase:firebase-auth-ktx:23.1.0")


    implementation("com.firebaseui:firebase-ui-auth:8.0.2")


    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")


    // Credentials Manager for Passkey support
    implementation("androidx.credentials:credentials:1.2.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")




    implementation("com.google.accompanist:accompanist-flowlayout:0.34.0")


    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
apply(plugin = "com.google.gms.google-services")
