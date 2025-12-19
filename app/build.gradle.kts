import org.gradle.kotlin.dsl.invoke

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.ediapp.mykeyword"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ediapp.mykeyword"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += "arm64-v8a"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

}
//
//repositories {
//    flatDir { dirs("libs") }
//}
//

dependencies {
//    implementation("com.github.bab2min:kiwi-android:0.16.0") // 최신 버전을 명시
//    implementation(files('libs/kiwi-java-v0.22.1-mac-arm64.jar'))
        // 파일명에서 버전 정보를 제외한 이름으로 지정합니다. (예: kiwi-android-0.15.1.aar -> kiwi-android)
//    implementation( 'kiwi-android-v0.2.22.1', ext: 'aar')
//    implementation(files("libs/kiwi-java-v0.22.1-mac-arm64.jar"))
    implementation(files("libs/kiwi-android-v0.22.1.aar"))
//    implementation("com.github.bab2min:kiwi-android:0.16.0")
//    implementation("kiwi-android-v0.22.1", ext = "aar")

    //    implementation(files("libs/kiwi-android-v0.2.22.1.aar"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3) // Add this line
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}
