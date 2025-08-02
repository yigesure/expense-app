plugins {
    kotlin("multiplatform") version "1.9.20"
    id("com.android.application") version "8.1.2"
    id("org.jetbrains.compose") version "1.5.4"
    id("app.cash.sqldelight") version "2.0.0"
    kotlin("plugin.serialization") version "1.9.20"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.uiTooling)
                implementation(compose.material3)
                implementation(compose.foundation)
                implementation(compose.animation)
                implementation(compose.runtime)
                
                // Android 特定依赖
                implementation("androidx.activity:activity-compose:1.8.0")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
                implementation("androidx.navigation:navigation-compose:2.7.4")
                implementation("androidx.biometric:biometric:1.1.0")
                
                // 加密和安全
                implementation("com.google.crypto.tink:tink-android:1.7.0")
                
                // 数据库
                implementation("app.cash.sqldelight:android-driver:2.0.0")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.0")
                
                // 网络和序列化
                implementation("io.ktor:ktor-client-android:2.3.5")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
                
                // 依赖注入
                implementation("io.insert-koin:koin-android:3.5.0")
                implementation("io.insert-koin:koin-androidx-compose:3.5.0")
                
                // 协程
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
                
                // 图标和动画
                implementation("androidx.compose.material:material-icons-extended:1.5.4")
                implementation("com.airbnb.android:lottie-compose:6.1.0")
            }
        }
        
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("app.cash.sqldelight:runtime:2.0.0")
                implementation("io.ktor:ktor-client-core:2.3.5")
            }
        }
    }
}

android {
    namespace = "com.passwordmanager.elite"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.passwordmanager.elite"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

sqldelight {
    databases {
        create("PasswordDatabase") {
            packageName.set("com.passwordmanager.elite.database")
            srcDirs.setFrom("src/commonMain/sqldelight")
        }
    }
}