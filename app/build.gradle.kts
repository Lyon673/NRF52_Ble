plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    // parcelable 序列化
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.mnrfble"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mnrfble"
        minSdk = 33
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
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
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

    implementation ("androidx.compose.ui:ui:1.6.6")
    implementation ("androidx.compose.material:material:1.6.6")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.6.6")
    implementation (libs.androidx.activity.compose.v172)
    implementation ("androidx.compose.material3:material3:1.2.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //协程库
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //Ble扫描库
    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")
    //Ble连接库
    implementation("no.nordicsemi.android.kotlin.ble:client:1.0.13")
    implementation("no.nordicsemi.android.kotlin.ble:server:1.0.13")
    implementation("no.nordicsemi.android:ble:2.7.2")
    implementation("no.nordicsemi.android:ble-ktx:2.7.2")

    implementation("io.github.cymchad:BaseRecyclerViewAdapterHelper:3.0.14")
    //viewBinding
    implementation("com.github.DylanCaiCoding.ViewBindingKTX:viewbinding-ktx:2.1.0")
    //jetpack-ktx
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    //hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
}