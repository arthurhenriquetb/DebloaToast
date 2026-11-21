plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "br.com.a01optimizer"
    compileSdk = 37
    signingConfigs {
        getByName("debug") {
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    defaultConfig {
        applicationId = "br.com.a01optimizer.v08"
        minSdk = 29
        targetSdk = 32
        versionCode = 17
        versionName = "0.12.2-expert"
    }
    packaging { resources { excludes.add("META-INF/LICENSE.md") } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation("com.flyfishxu:kadb:2.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
