plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.anish.jarvis"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.anish.jarvis"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "3.0"
        ndk {
            // real phones only (smaller APK)
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    // Release signing: uses YOUR private key when the JARVIS_KEYSTORE_* secrets are set in GitHub,
    // otherwise falls back to the public key in this repo (so updates still install).
    val ksPath = System.getenv("JARVIS_KEYSTORE_FILE")
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            if (!ksPath.isNullOrBlank()) {
                storeFile = file(ksPath)
                storePassword = System.getenv("JARVIS_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("JARVIS_KEY_ALIAS")
                keyPassword = System.getenv("JARVIS_KEY_PASSWORD")
            } else {
                storeFile = file("debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            // not debuggable: nobody can attach a debugger or read Jarvis's private files over USB
            isDebuggable = false
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    androidResources {
        // keep the bundled model zips as-is
        noCompress += listOf("zip")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    // Vosk: fully offline speech recognition + speaker identification
    implementation("com.alphacephei:vosk-android:0.3.47@aar")
    implementation("net.java.dev.jna:jna:5.13.0@aar")

    testImplementation("junit:junit:4.13.2")
}
