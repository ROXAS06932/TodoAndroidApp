plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.websarva.wings.android.todoaprication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.websarva.wings.android.todoaprication"
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
}

dependencies {

    //Room適用処理
    implementation("androidx.room:room-runtime:2.5.0")
    implementation(libs.room.runtime.android)
    annotationProcessor("androidx.room:room-compiler:2.5.0")
    //Gson適用処理↓
    implementation("com.google.code.gson:gson:2.8.9")
//    implementation(fileTree("libs") {include("*.jar")})
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
